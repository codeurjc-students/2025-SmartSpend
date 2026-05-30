import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { NavBarComponent } from './nav-bar.component';

describe('NavBarComponent', () => {
  let component: NavBarComponent;
  let fixture: ComponentFixture<NavBarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NavBarComponent],
      providers: [provideRouter([])]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NavBarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should toggle the mobile menu', () => {
    const menuButton: HTMLButtonElement = fixture.nativeElement.querySelector('.menu-toggle');

    expect(menuButton).toBeTruthy();
    expect(component.isMobileMenuOpen).toBeFalse();

    menuButton.click();
    fixture.detectChanges();

    expect(component.isMobileMenuOpen).toBeTrue();
    expect(fixture.nativeElement.querySelector('.mobile-menu')).toBeTruthy();
  });

  it('should close the mobile menu from the close button', () => {
    component.isMobileMenuOpen = true;
    fixture.detectChanges();

    const closeButton: HTMLButtonElement = fixture.nativeElement.querySelector('.mobile-menu-close');

    expect(closeButton).toBeTruthy();

    closeButton.click();
    fixture.detectChanges();

    expect(component.isMobileMenuOpen).toBeFalse();
    expect(fixture.nativeElement.querySelector('.mobile-menu')).toBeFalsy();
  });
});
