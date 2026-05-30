import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BehaviorSubject, of } from 'rxjs';

import { CreateTransactionModalComponent } from './create-transaction-modal.component';
import { TransactionService } from '../../services/transaction/transaction.service';
import { CategoryService } from '../../services/category/category.service';
import { ActiveAccountService } from '../../services/active-account/active-account.service';
import { BankAccountServiceService } from '../../services/bankAccount/bank-account-service.service';

describe('CreateTransactionModalComponent', () => {
  let component: CreateTransactionModalComponent;
  let fixture: ComponentFixture<CreateTransactionModalComponent>;
  const activeAccountSubject = new BehaviorSubject<any>(null);

  const transactionServiceMock = {
    createTransaction: jasmine.createSpy().and.returnValue(of({})),
    createTransactionWithImage: jasmine.createSpy().and.returnValue(of({})),
    createTransfer: jasmine.createSpy().and.returnValue(of({})),
    updateTransaction: jasmine.createSpy().and.returnValue(of({}))
  };

  const categoryServiceMock = {
    getCategoriesForType: jasmine.createSpy().and.returnValue(of([]))
  };

  const activeAccountServiceMock = {
    activeAccount$: activeAccountSubject.asObservable(),
    getActiveAccount: () => activeAccountSubject.value,
    getActiveAccountValue: () => activeAccountSubject.value
  };

  const bankAccountServiceMock = {
    getBankAccounts: jasmine.createSpy().and.returnValue(of([]))
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateTransactionModalComponent],
      providers: [
        { provide: TransactionService, useValue: transactionServiceMock },
        { provide: CategoryService, useValue: categoryServiceMock },
        { provide: ActiveAccountService, useValue: activeAccountServiceMock },
        { provide: BankAccountServiceService, useValue: bankAccountServiceMock }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CreateTransactionModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
