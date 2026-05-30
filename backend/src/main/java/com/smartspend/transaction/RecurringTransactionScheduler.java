package com.smartspend.transaction;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.smartspend.bankAccount.BankAccount;
import com.smartspend.bankAccount.BankAccountRepository;

@Component
public class RecurringTransactionScheduler {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private TransactionService transactionService;

    @Scheduled(cron = "0 0 0 * * ?") 
    @Transactional
    public void generateRecurringTransactions() {
        LocalDate today = LocalDate.now();
        System.out.println("Ejecutando scheduler de transacciones recurrentes para fecha: " + today);

        List<Transaction> pendingTransactions = transactionRepository.findPendingRecurringTransactions(today);

        if (pendingTransactions.isEmpty()){
            System.out.println("Sin transacciones recurrentes pendientes para hoy");
            return;
        } 

        System.out.println("Encontradas " + pendingTransactions.size() + " transacciones recurrentes pendientes:");

        for (Transaction parentTransaction : pendingTransactions){
            try {
                System.out.println("Procesando: '" + parentTransaction.getTitle() + "' (próxima fecha: " + parentTransaction.getNextRecurrenceDate() + ")");
                
                // Bucle while para catch-up: generar todos los hijos atrasados de una vez
                while (parentTransaction.getNextRecurrenceDate() != null && 
                       parentTransaction.getNextRecurrenceDate().compareTo(today) <= 0) {
                    
                    // Verificar si se alcanzó la fecha de fin de recurrencia
                    if (parentTransaction.getRecurrenceEndDate() != null &&
                        parentTransaction.getNextRecurrenceDate().isAfter(parentTransaction.getRecurrenceEndDate())) {
                        System.out.println("Recurrencia finalizada para: '" + parentTransaction.getTitle() + "'. Aplicando borrado lógico.");
                        cancelRecurrence(parentTransaction);
                        break;
                    }
                    
                    generateChildTransaction(parentTransaction);
                    updateNextRecurrenceDate(parentTransaction);
                }
                
                System.out.println("Generada(s) exitosamente: '" + parentTransaction.getTitle() + "'");
            } catch (Exception e) {
                System.out.println("Error generando transaccion recurrente '" + parentTransaction.getTitle() + "': " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("Scheduler completado para " + today);
    }

    private void generateChildTransaction(Transaction parent){

        Transaction child = Transaction.builder()
            .title(parent.getTitle())
            .description(parent.getDescription())
            .amount(parent.getAmount())
            .date(parent.getNextRecurrenceDate()) 
            .type(parent.getType())
            .recurrence(Recurrence.NONE) 
            .category(parent.getCategory())
            .account(parent.getAccount())
            .beforeBalance(parent.getAccount().getCurrentBalance())
            .isRecurringSeriesParent(false) 
            .nextRecurrenceDate(null)
            .parentTransaction(parent)
            .imageData(parent.getImageData())
            .imageName(parent.getImageName())
            .imageType(parent.getImageType())
            .build();

        transactionService.upadateAccountBalance(child, parent.getAccount());
        bankAccountRepository.save(parent.getAccount());
        transactionRepository.save(child);
    }

    private void updateNextRecurrenceDate(Transaction parent) {
        LocalDate currentNext = parent.getNextRecurrenceDate();
        LocalDate newNext = calculateNextRecurrenceDate(currentNext, parent.getRecurrence());
        
        parent.setNextRecurrenceDate(newNext);
        transactionRepository.save(parent);
    }

    private LocalDate calculateNextRecurrenceDate(LocalDate currentDate, Recurrence recurrenceType) {
        return switch (recurrenceType) {
            case DAILY -> currentDate.plusDays(1);
            case WEEKLY -> currentDate.plusWeeks(1);
            case MONTHLY -> currentDate.plusMonths(1);
            case YEARLY -> currentDate.plusYears(1);
            case NONE -> null;
        };
    }

    private void cancelRecurrence(Transaction parent) {
        parent.setIsRecurringSeriesParent(false);
        parent.setRecurrence(Recurrence.NONE);
        parent.setNextRecurrenceDate(null);
        transactionRepository.save(parent);
    }

}
