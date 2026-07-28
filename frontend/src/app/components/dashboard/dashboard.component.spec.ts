import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DashboardComponent } from './dashboard.component';
import { Transaction } from '../../interfaces/transaction.interface';

describe('DashboardComponent - Date Logic', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;

  beforeEach(async () => {
    // Create a minimal test component that doesn't call real services
    const MockDashboardComponent = DashboardComponent;

    await TestBed.configureTestingModule({
      imports: [MockDashboardComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
  });

  describe('isPaidThisMonth method', () => {
    it('should return true when nextRecurrenceDate is in next month', () => {
      const tx: Transaction = {
        id: 1,
        title: 'Test',
        amount: 100,
        date: '',
        type: 'EXPENSE',
        recurrence: 'MONTHLY',
        accountId: 1,
        accountName: 'Test',
        category: { id: 1, name: 'Test', color: '', icon: '', type: 'EXPENSE', isDefault: false },
        hasImage: false,
        nextRecurrenceDate: '2026-08-15'  // Next month
      };

      expect(component.isPaidThisMonth(tx)).toBe(true);
    });

    it('should return false when nextRecurrenceDate is in current month', () => {
      const tx: Transaction = {
        id: 1,
        title: 'Test',
        amount: 100,
        date: '',
        type: 'EXPENSE',
        recurrence: 'MONTHLY',
        accountId: 1,
        accountName: 'Test',
        category: { id: 1, name: 'Test', color: '', icon: '', type: 'EXPENSE', isDefault: false },
        hasImage: false,
        nextRecurrenceDate: '2026-07-15'  // Current month
      };

      expect(component.isPaidThisMonth(tx)).toBe(false);
    });

    it('should return false when nextRecurrenceDate is null', () => {
      const tx: Transaction = {
        id: 1,
        title: 'Test',
        amount: 100,
        date: '',
        type: 'EXPENSE',
        recurrence: 'NONE',
        accountId: 1,
        accountName: 'Test',
        category: { id: 1, name: 'Test', color: '', icon: '', type: 'EXPENSE', isDefault: false },
        hasImage: false,
        nextRecurrenceDate: null
      };

      expect(component.isPaidThisMonth(tx)).toBe(false);
    });
  });

  describe('getOccurrenceDateThisMonth method', () => {
    it('should return nextRecurrenceDate when pending', () => {
      const tx: Transaction = {
        id: 1,
        title: 'Test',
        amount: 100,
        date: '2025-01-15',
        type: 'EXPENSE',
        recurrence: 'MONTHLY',
        accountId: 1,
        accountName: 'Test',
        category: { id: 1, name: 'Test', color: '', icon: '', type: 'EXPENSE', isDefault: false },
        hasImage: false,
        nextRecurrenceDate: '2026-07-20'
      };

      const result = component.getOccurrenceDateThisMonth(tx);
      expect(result.getDate()).toBe(20);
    });

    it('should reconstruct date in current month when paid', () => {
      const tx: Transaction = {
        id: 1,
        title: 'Test',
        amount: 100,
        date: '2024-06-25',
        type: 'EXPENSE',
        recurrence: 'MONTHLY',
        accountId: 1,
        accountName: 'Test',
        category: { id: 1, name: 'Test', color: '', icon: '', type: 'EXPENSE', isDefault: false },
        hasImage: false,
        nextRecurrenceDate: '2026-08-25'  // Next month = paid
      };

      const result = component.getOccurrenceDateThisMonth(tx);
      expect(result.getDate()).toBe(25);  // Day from nextRecurrenceDate
    });

    it('should fall back to original date when null', () => {
      const tx: Transaction = {
        id: 1,
        title: 'Test',
        amount: 100,
        date: '2026-06-10',
        type: 'EXPENSE',
        recurrence: 'NONE',
        accountId: 1,
        accountName: 'Test',
        category: { id: 1, name: 'Test', color: '', icon: '', type: 'EXPENSE', isDefault: false },
        hasImage: false,
        nextRecurrenceDate: null
      };

      const result = component.getOccurrenceDateThisMonth(tx);
      expect(result.getFullYear()).toBe(2026);
      expect(result.getMonth()).toBe(5);  // June
      expect(result.getDate()).toBe(10);
    });
  });
});
