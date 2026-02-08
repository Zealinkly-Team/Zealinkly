package com.hyan.zealinklybackend.service;

import com.hyan.zealinklybackend.entity.EmergencyAlarm;
import com.hyan.zealinklybackend.entity.Elder;
import com.hyan.zealinklybackend.repository.EmergencyAlarmRepository;
import com.hyan.zealinklybackend.repository.ElderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmergencyService {

    private final EmergencyAlarmRepository alarmRepository;
    private final ElderRepository elderRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // 老人触发报警
    public void triggerAlarm(Long elderId, String location) {
        Elder elder = elderRepository.findById(elderId)
                .orElseThrow(() -> new RuntimeException("老人信息不存在"));

        EmergencyAlarm alarm = new EmergencyAlarm();
        alarm.setElder(elder);
        alarm.setLocation(location);
        alarm.setStatus("PENDING");
        alarm.setCreatedAt(LocalDateTime.now());

        alarmRepository.save(alarm);

        // 【关键步】通过 WebSocket 向所有订阅了 /topic/alarms 的管理员推送实时消息
        messagingTemplate.convertAndSend("/topic/alarms", "紧急报警：" + elder.getRealName() + " 在 " + location + " 发起求助！");
    }

    // 管理员处理报警
    public void handleAlarm(Long alarmId, String note) {
        EmergencyAlarm alarm = alarmRepository.findById(alarmId).get();
        alarm.setStatus("HANDLED");
        alarm.setHandledAt(LocalDateTime.now());
        alarm.setHandleNote(note);
        alarmRepository.save(alarm);
    }
}