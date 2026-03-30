package com.project.eventplan.Service;

import com.project.eventplan.Config.SecurityUtil;
import com.project.eventplan.Entity.Event;
import com.project.eventplan.Entity.Task;
import com.project.eventplan.Entity.User;
import com.project.eventplan.Exception.BadRequestException;
import com.project.eventplan.Exception.ResourceNotFoundException;
import com.project.eventplan.Repository.EventRepository;
import com.project.eventplan.Repository.TaskRepository;
import com.project.eventplan.Repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;

    public TaskService(TaskRepository taskRepository,
                       EventRepository eventRepository,
                       UserRepository userRepository,
                       SecurityUtil securityUtil) {
        this.taskRepository = taskRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.securityUtil = securityUtil;
    }

    public Task createTask(Long eventId,
                           Long teamMemberId,
                           Long vendorId,
                           Task task) {
        if (eventId == null) {
            throw new BadRequestException("eventId is required");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        String currentEmail = securityUtil.getCurrentUserEmail();
        if (!event.getOrganizer().getEmail().equals(currentEmail)) {
            throw new AccessDeniedException("Not your event");
        }

        User teamMember = null;
        if (teamMemberId != null) {
            teamMember = userRepository.findById(teamMemberId)
                    .orElseThrow(() -> new ResourceNotFoundException("Team member not found"));

            if (!"TEAM_MEMBER".equals(teamMember.getRole().name())) {
                throw new BadRequestException("User is not a team member");
            }
        }

        User vendor = null;
        if (vendorId != null) {
            vendor = userRepository.findById(vendorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

            if (!"VENDOR".equals(vendor.getRole().name())) {
                throw new BadRequestException("User is not a vendor");
            }
        }

        task.setEvent(event);
        task.setAssignedTeamMember(teamMember);
        task.setAssignedVendor(vendor);
        task.setStatus("NOT_STARTED");
        return taskRepository.save(task);
    }

    public Task getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        String email = securityUtil.getCurrentUserEmail();
        if (task.getEvent().getOrganizer().getEmail().equals(email)
                || (task.getAssignedTeamMember() != null && task.getAssignedTeamMember().getEmail().equals(email))
                || (task.getAssignedVendor() != null && task.getAssignedVendor().getEmail().equals(email))) {
            return task;
        }

        throw new AccessDeniedException("Access denied");
    }

    public List<Task> getTasksForCurrentUser() {
        String email = securityUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return switch (user.getRole()) {
            case ORGANIZER -> taskRepository.findByEvent_Organizer_Email(email);
            case TEAM_MEMBER -> taskRepository.findByAssignedTeamMember_Email(email);
            case VENDOR -> taskRepository.findByAssignedVendor_Email(email);
        };
    }

    public Task updateTaskStatus(Long taskId, String status) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        String email = securityUtil.getCurrentUserEmail();
        if (task.getAssignedTeamMember() == null || !task.getAssignedTeamMember().getEmail().equals(email)) {
            throw new AccessDeniedException("Not your task");
        }

        task.setStatus(status);
        return taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        String email = securityUtil.getCurrentUserEmail();
        if (!task.getEvent().getOrganizer().getEmail().equals(email)) {
            throw new AccessDeniedException("Not your event");
        }

        taskRepository.delete(task);
    }
}
