package com.jsalva.trainerworkload.messaging.consumer;

import com.jsalva.trainerworkload.dto.request.TrainerWorkloadCommandMessageDto;
import com.jsalva.trainerworkload.enums.ActionType;
import com.jsalva.trainerworkload.service.TrainerWorkloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class TrainerWorkloadConsumer {
    private final Logger logger = LoggerFactory.getLogger(TrainerWorkloadConsumer.class);

    private final TrainerWorkloadService trainerWorkloadService;

    private final JmsTemplate jmsTemplate;


    public TrainerWorkloadConsumer(TrainerWorkloadService trainerWorkloadService, JmsTemplate jmsTemplate) {
        this.trainerWorkloadService = trainerWorkloadService;
        this.jmsTemplate = jmsTemplate;
    }

    @JmsListener(destination = "trainer.workload.command.queue")
    public void receiveTrainerWorkloadCommandMessage(
            @Payload TrainerWorkloadCommandMessageDto messageDto,
            @Header("X-Transaction-Id") String transactionId,
            @Header("X-Action-Type") String actionType){

        System.out.println("Received message: username "+ messageDto.username());

        // Test error Handling and Dead Letter
        if(messageDto.username().equals("Error.Test")){
            throw new IllegalArgumentException("Error, Invalid Name");
        }

        // Extract headers
        try {
            // Set MDC for logging
            MDC.put("transactionId", transactionId);

            logger.info("Received workload message. Trainer={}, Action={}, TxId={}",
                    messageDto.username(), actionType, transactionId);

            // Validate action type
            ActionType action = ActionType.valueOf(actionType);

            // Route based on action type
            switch (action) {
                case ADD, DELETE -> trainerWorkloadService.updateWorkload(messageDto, action);
                default -> {
                    logger.error("Invalid action type: {}", actionType);
                    throw new IllegalArgumentException("Invalid action type: " + actionType);
                }
            }

            logger.info("Successfully processed workload message. Trainer={}, Action={}",
                    messageDto.username(), actionType);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid message format. TxId={}", transactionId, e);
            throw new RuntimeException("Invalid message", e);  // Goes to DLQ

        } finally {
            MDC.clear();
        }
    }
}
