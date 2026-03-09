import React, { useState } from "react";
import axios from "axios";
import "./Chatbot.css";

function Chatbot() {

  const [open, setOpen] = useState(false);
  const [message, setMessage] = useState("");
  const [chat, setChat] = useState([]);

  // ✅ Gemini API Key
  const API_KEY = "AIzaSyCjhKNFRdRrUCLfNM_lBmGO3-MQbtotcZk";

  // ✅ Send Message Function
  const sendMessage = async () => {

  if (!message.trim()) return;

  const userText = message;

  setChat(prev => [
    ...prev,
    { sender: "user", text: userText }
  ]);

  setMessage("");

  try {

    const response = await axios.post(
      `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${API_KEY}`,
      {
        contents: [
          {
            role: "user",
            parts: [{ text: userText }]
          }
        ]
      },
      {
        headers: {
          "Content-Type": "application/json"
        }
      }
    );

    console.log(response.data); // ✅ DEBUG

    const botReply =
      response?.data?.candidates?.[0]?.content?.parts?.[0]?.text
      || "No response from AI";

    setChat(prev => [
      ...prev,
      { sender: "bot", text: botReply }
    ]);

  } catch (error) {

    console.error("Gemini Error:", error.response || error);

    setChat(prev => [
      ...prev,
      {
        sender: "bot",
        text: "⚠️ Unable to get response from AI."
      }
    ]);
  }
};

  return (
    <>
      {/* ✅ Floating Chat Icon */}
      <div
        className="chat-icon"
        onClick={() => setOpen(!open)}
      >
        💬
      </div>

      {/* ✅ Chat Window */}
      {open && (
        <div className="chat-window">

          <div className="chat-header">
            Grocery Assistant 🤖
          </div>

          <div className="chat-body">
            {chat.map((msg, index) => (
              <div
                key={index}
                className={
                  msg.sender === "user"
                    ? "user-msg"
                    : "bot-msg"
                }
              >
                {msg.text}
              </div>
            ))}
          </div>

          <div className="chat-input">
            <input
              type="text"
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              placeholder="Ask about groceries..."
              onKeyDown={(e) =>
                e.key === "Enter" && sendMessage()
              }
            />

            <button onClick={sendMessage}>
              Send
            </button>
          </div>

        </div>
      )}
    </>
  );
}

export default Chatbot;