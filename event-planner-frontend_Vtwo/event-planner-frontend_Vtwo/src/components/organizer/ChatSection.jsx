import { useState, useEffect, useRef } from 'react';
import api from '../../services/api';
import { extractItems } from '../../utils/auth';
import { useAuth } from '../../context/AuthContext';
import SectionCard from '../SectionCard';
import Modal from '../Modal';

export default function ChatSection() {
  const { user } = useAuth();
  const [chats, setChats] = useState([]);
  const [activeChat, setActiveChat] = useState(null);
  const [messages, setMessages] = useState([]);
  const [participants, setParticipants] = useState([]);
  const [events, setEvents] = useState([]);
  const [users, setUsers] = useState([]);
  const [newMsg, setNewMsg] = useState('');
  const [createModal, setCreateModal] = useState(false);
  const [addParticipantModal, setAddParticipantModal] = useState(false);
  const [chatForm, setChatForm] = useState({ chatName: '', chatType: 'GROUP', eventId: '' });
  const [participantUserId, setParticipantUserId] = useState('');
  const [loading, setLoading] = useState(true);
  const [msgLoading, setMsgLoading] = useState(false);
  const bottomRef = useRef(null);

  const loadChats = async () => {
    setLoading(true);
    try {
      const [cRes, eRes, uRes] = await Promise.all([api.get('/chats'), api.get('/events'), api.get('/users')]);
      setChats(extractItems(cRes.data));
      setEvents(extractItems(eRes.data));
      setUsers(extractItems(uRes.data));
    } catch {}
    finally { setLoading(false); }
  };

  const loadMessages = async (chatId) => {
    setMsgLoading(true);
    try {
      const [mRes, pRes] = await Promise.all([api.get(`/messages/chat/${chatId}`), api.get(`/chat-participants/chat/${chatId}`)]);
      setMessages(extractItems(mRes.data));
      setParticipants(extractItems(pRes.data));
    } catch {}
    finally { setMsgLoading(false); }
  };

  useEffect(() => { loadChats(); }, []);

  useEffect(() => {
    if (activeChat) loadMessages(activeChat.id || activeChat.chatId);
  }, [activeChat]);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const sendMessage = async (e) => {
    e.preventDefault();
    if (!newMsg.trim() || !activeChat) return;
    const chatId = activeChat.id || activeChat.chatId;
    try {
      await api.post('/messages', { content: newMsg, chatId });
      setNewMsg('');
      loadMessages(chatId);
    } catch {}
  };

  const createChat = async (e) => {
    e.preventDefault();
    try {
      await api.post('/chats', chatForm);
      setCreateModal(false);
      loadChats();
    } catch {}
  };

  const addParticipant = async (e) => {
    e.preventDefault();
    if (!activeChat || !participantUserId) return;
    try {
      await api.post('/chat-participants', { chatId: activeChat.id || activeChat.chatId, userId: participantUserId });
      setAddParticipantModal(false);
      loadMessages(activeChat.id || activeChat.chatId);
    } catch {}
  };

  const removeParticipant = async (pId) => {
    try { await api.delete(`/chat-participants/${pId}`); loadMessages(activeChat.id || activeChat.chatId); } catch {}
  };

  const getUserName = (id) => users.find((u) => (u.userId || u.id) === id)?.name || `User ${id}`;

  return (
    <div className="h-[calc(100vh-8rem)] flex gap-4">
      {/* Chat list */}
      <div className="w-64 shrink-0 card flex flex-col overflow-hidden">
        <div className="flex items-center justify-between px-4 py-3 border-b border-surface-100">
          <h3 className="font-display text-base text-surface-800">Chats</h3>
          <button className="btn-primary text-xs py-1 px-2.5" onClick={() => { setChatForm({ chatName: '', chatType: 'GROUP', eventId: '' }); setCreateModal(true); }}>+ New</button>
        </div>
        <div className="flex-1 overflow-y-auto divide-y divide-surface-100">
          {loading ? (
            <div className="flex items-center justify-center h-24">
              <div className="w-5 h-5 border-2 border-primary-500 border-t-transparent rounded-full animate-spin" />
            </div>
          ) : chats.length === 0 ? (
            <p className="text-center text-sm text-slate-400 py-8">No chats yet.</p>
          ) : (
            chats.map((chat) => (
              <button
                key={chat.id || chat.chatId}
                onClick={() => setActiveChat(chat)}
                className={`w-full text-left px-4 py-3 hover:bg-surface-50 transition-colors ${(activeChat?.id || activeChat?.chatId) === (chat.id || chat.chatId) ? 'bg-primary-50' : ''}`}
              >
                <p className={`text-sm font-medium ${(activeChat?.id || activeChat?.chatId) === (chat.id || chat.chatId) ? 'text-primary-700' : 'text-slate-700'}`}>{chat.chatName}</p>
                <p className="text-xs text-slate-400 mt-0.5">{chat.chatType}</p>
              </button>
            ))
          )}
        </div>
      </div>

      {/* Message area */}
      <div className="flex-1 card flex flex-col overflow-hidden">
        {!activeChat ? (
          <div className="flex-1 flex items-center justify-center text-slate-400">
            <div className="text-center">
              <span className="text-4xl">💬</span>
              <p className="mt-2 text-sm">Select a chat to start messaging</p>
            </div>
          </div>
        ) : (
          <>
            {/* Chat header */}
            <div className="flex items-center justify-between px-5 py-3 border-b border-surface-100">
              <div>
                <h3 className="font-medium text-slate-800">{activeChat.chatName}</h3>
                <p className="text-xs text-slate-400">{participants.length} participants</p>
              </div>
              <button className="btn-secondary text-xs py-1.5" onClick={() => { setParticipantUserId(''); setAddParticipantModal(true); }}>+ Participant</button>
            </div>

            {/* Messages */}
            <div className="flex-1 overflow-y-auto p-4 space-y-3">
              {msgLoading ? (
                <div className="flex items-center justify-center h-16">
                  <div className="w-5 h-5 border-2 border-primary-500 border-t-transparent rounded-full animate-spin" />
                </div>
              ) : messages.length === 0 ? (
                <p className="text-center text-sm text-slate-400 py-8">No messages yet. Start the conversation!</p>
              ) : (
                messages.map((msg, i) => {
                  const isMe = String(msg.senderId || msg.userId) === String(user?.userId || user?.id);
                  return (
                    <div key={msg.id || i} className={`flex ${isMe ? 'justify-end' : 'justify-start'}`}>
                      <div className={`max-w-xs lg:max-w-sm px-3 py-2 rounded-xl text-sm ${isMe ? 'bg-primary-600 text-white' : 'bg-surface-100 text-slate-700'}`}>
                        {!isMe && <p className="text-xs font-semibold mb-0.5 text-primary-600">{getUserName(msg.senderId || msg.userId)}</p>}
                        <p>{msg.content}</p>
                      </div>
                    </div>
                  );
                })
              )}
              <div ref={bottomRef} />
            </div>

            {/* Input */}
            <form onSubmit={sendMessage} className="flex gap-2 p-3 border-t border-surface-100">
              <input
                className="input-field flex-1"
                placeholder="Type a message..."
                value={newMsg}
                onChange={(e) => setNewMsg(e.target.value)}
              />
              <button type="submit" className="btn-primary px-4">Send</button>
            </form>
          </>
        )}
      </div>

      {/* Create Chat Modal */}
      <Modal isOpen={createModal} onClose={() => setCreateModal(false)} title="Create Chat" size="sm">
        <form onSubmit={createChat} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide">Chat Name</label>
            <input className="input-field" value={chatForm.chatName} onChange={(e) => setChatForm({ ...chatForm, chatName: e.target.value })} required />
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide">Type</label>
            <select className="input-field" value={chatForm.chatType} onChange={(e) => setChatForm({ ...chatForm, chatType: e.target.value })}>
              <option value="GROUP">GROUP</option>
              <option value="DIRECT">DIRECT</option>
            </select>
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide">Event (optional)</label>
            <select className="input-field" value={chatForm.eventId} onChange={(e) => setChatForm({ ...chatForm, eventId: e.target.value })}>
              <option value="">None</option>
              {events.map((ev) => <option key={ev.id || ev.eventId} value={ev.id || ev.eventId}>{ev.eventName}</option>)}
            </select>
          </div>
          <div className="flex gap-3">
            <button type="submit" className="btn-primary flex-1">Create</button>
            <button type="button" onClick={() => setCreateModal(false)} className="btn-secondary flex-1">Cancel</button>
          </div>
        </form>
      </Modal>

      {/* Add Participant Modal */}
      <Modal isOpen={addParticipantModal} onClose={() => setAddParticipantModal(false)} title="Add Participant" size="sm">
        <form onSubmit={addParticipant} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide">User</label>
            <select className="input-field" value={participantUserId} onChange={(e) => setParticipantUserId(e.target.value)} required>
              <option value="">Select user...</option>
              {users.map((u) => <option key={u.userId || u.id} value={u.userId || u.id}>{u.name} ({u.role})</option>)}
            </select>
          </div>
          <div className="flex gap-3">
            <button type="submit" className="btn-primary flex-1">Add</button>
            <button type="button" onClick={() => setAddParticipantModal(false)} className="btn-secondary flex-1">Cancel</button>
          </div>
        </form>
        {participants.length > 0 && (
          <div className="mt-4 border-t border-surface-100 pt-4">
            <p className="text-xs font-semibold text-slate-500 mb-2 uppercase tracking-wide">Current Participants</p>
            <ul className="space-y-1">
              {participants.map((p) => (
                <li key={p.id} className="flex items-center justify-between text-sm text-slate-600">
                  <span>{getUserName(p.userId)}</span>
                  <button onClick={() => removeParticipant(p.id)} className="text-red-400 hover:text-red-600 text-xs">Remove</button>
                </li>
              ))}
            </ul>
          </div>
        )}
      </Modal>
    </div>
  );
}
