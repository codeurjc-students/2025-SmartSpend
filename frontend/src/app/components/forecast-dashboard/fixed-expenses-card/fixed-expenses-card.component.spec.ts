import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FixedExpensesCardComponent } from './fixed-expenses-card.component';
import { Transaction } from '../../../interfaces/transaction.interface';

describe('FixedExpensesCardComponent', () => {
  let component: FixedExpensesCardComponent;
  let fixture: ComponentFixture<FixedExpensesCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FixedExpensesCardComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FixedExpensesCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('isPaidThisMonth', () => {
    it('should return true when nextRecurrenceDate is in next month', () => {
      const now = new Date();
      // Set to August 15, 2026
      const nextMonth = new Date(2026, 7, 15);
      const nextMonthString = '2026-08-15';

      const tx: Transaction = {
        id: 1,
        title: 'Netflix',
        amount: 15.99,
        date: new Date().toISOString(),
        type: 'EXPENSE',
        recurrence: 'MONTHLY',
        accountId: 1,
        accountName: 'Test',
        category: { id: 1, name: 'Sub', color: '#f00', icon: '💳', type: 'EXPENSE', isDefault: false },
        hasImage: false,
        isRecurringSeriesParent: true,
        nextRecurrenceDate: nextMonthString
      };

      expect(component.isPaidThisMonth(tx)).toBe(true);
    });

    it('should return false when nextRecurrenceDate is in current month', () => {
      const now = new Date();
      const currentMonthString = now.toISOString().split('T')[0]; // Today

      const tx: Transaction = {
        id: 1,
        title: 'Spotify',
        amount: 12.99,
        date: new Date().toISOString(),
        type: 'EXPENSE',
        recurrence: 'MONTHLY',
        accountId: 1,
        accountName: 'Test',
        category: { id: 1, name: 'Sub', color: '#1db954', icon: '🎵', type: 'EXPENSE', isDefault: false },
        hasImage: false,
        isRecurringSeriesParent: true,
        nextRecurrenceDate: currentMonthString
      };

      expect(component.isPaidThisMonth(tx)).toBe(false);
    });

    it('should return false when nextRecurrenceDate is null', () => {
      const tx: Transaction = {
        id: 1,
        title: 'Regular',
        amount: 50,
        date: new Date().toISOString(),
        type: 'EXPENSE',
        recurrence: 'NONE',
        accountId: 1,
        accountName: 'Test',
        category: { id: 1, name: 'Test', color: '#999', icon: '📌', type: 'EXPENSE', isDefault: false },
        hasImage: false,
        nextRecurrenceDate: null
      };

      expect(component.isPaidThisMonth(tx)).toBe(false);
    });
  });

  describe('getOccurrenceDateThisMonth', () => {
    it('should return nextRecurrenceDate when pending', () => {
      const tx: Transaction = {
        id: 1,
        title: 'Test',
        amount: 10,
        date: '2025-01-15',
        type: 'EXPENSE',
        recurrence: 'MONTHLY',
        accountId: 1,
        accountName: 'Test',
        category: { id: 1, name: 'Test', color: '#000', icon: '📌', type: 'EXPENSE', isDefault: false },
        hasImage: false,
        nextRecurrenceDate: '2026-07-25'  // This month
      };

      const result = component.getOccurrenceDateThisMonth(tx);
      expect(result.getDate()).toBe(25);
    });

    it('should reconstruct date when paid', () => {
      const tx: Transaction = {
        id: 1,
        title: 'Test',
        amount: 10,
        date: '2024-06-20',
        type: 'EXPENSE',
        recurrence: 'MONTHLY',
        accountId: 1,
        accountName: 'Test',
        category: { id: 1, name: 'Test', color: '#000', icon: '📌', type: 'EXPENSE', isDefault: false },
        hasImage: false,
        nextRecurrenceDate: '2026-08-20'  // Next month = paid
      };

      const result = component.getOccurrenceDateThisMonth(tx);
      // Should be day 20 in current month
      expect(result.getDate()).toBe(20);
    });

    it('should fall back to original date when no nextRecurrenceDate', () => {
      const tx: Transaction = {
        id: 1,
        title: 'Test',
        amount: 10,
        date: '2026-05-15',
        type: 'EXPENSE',
        recurrence: 'NONE',
        accountId: 1,
        accountName: 'Test',
        category: { id: 1, name: 'Test', color: '#000', icon: '📌', type: 'EXPENSE', isDefault: false },
        hasImage: false,
        nextRecurrenceDate: null
      };

      const result = component.getOccurrenceDateThisMonth(tx);
      expect(result.getFullYear()).toBe(2026);
      expect(result.getMonth()).toBe(4); // May (0-indexed)
      expect(result.getDate()).toBe(15);
    });
  });

  describe('totalFixedExpenses', () => {
    it('should calculate sum of fixed expenses', () => {
      component.fixedExpenses = [
        { id: 1, title: 'A', amount: 10, date: '', type: 'EXPENSE', recurrence: 'MONTHLY', accountId: 1, accountName: '', category: { id: 1, name: '', color: '', icon: '', type: 'EXPENSE', isDefault: false }, hasImage: false },
        { id: 2, title: 'B', amount: 20, date: '', type: 'EXPENSE', recurrence: 'MONTHLY', accountId: 1, accountName: '', category: { id: 1, name: '', color: '', icon: '', type: 'EXPENSE', isDefault: false }, hasImage: false }
      ];

      expect(component.totalFixedExpenses).toBe(30);
    });

    it('should return 0 when empty', () => {
      component.fixedExpenses = [];
      expect(component.totalFixedExpenses).toBe(0);
    });
  });
});
