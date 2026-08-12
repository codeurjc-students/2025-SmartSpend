import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

interface OnboardingStep {
  icon: string;
  title: string;
  description: string;
  details: string[];
  tip?: string;
}

const ONBOARDING_KEY = 'smartspend_onboarding_done';

@Component({
  selector: 'app-onboarding',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './onboarding.component.html',
  styleUrl: './onboarding.component.css'
})
export class OnboardingComponent {
  @Output() closed = new EventEmitter<void>();

  currentStep = 0;

  readonly steps: OnboardingStep[] = [
    {
      icon: '👋',
      title: '¡Bienvenido a SmartSpend!',
      description: 'Tu gestor de finanzas personales inteligente. En menos de 2 minutos conocerás todo lo que puedes hacer.',
      details: [
        'Controla tus gastos e ingresos en un solo lugar',
        'Divide gastos compartidos y salda deudas fácilmente',
        'Automatiza transacciones que se repiten cada mes',
        'Visualiza tu salud financiera con gráficos y previsiones'
      ]
    },
    {
      icon: '🏦',
      title: 'Cuentas bancarias',
      description: 'Todo gira en torno a tus cuentas. Crea una cuenta por cada banco o propósito que quieras controlar.',
      details: [
        'Crea varias cuentas: cuenta corriente, ahorro...',
        'Cada cuenta tiene su propio saldo e historial de movimientos',
        'Cambia de cuenta activa desde el panel principal',
        'El saldo se actualiza automáticamente con cada transacción',
        'Además podrás hacer movimientos entre tus cuentas sin romper tus estadísticas'
      ],
      tip: 'Empieza con una sola cuenta y añade más cuando lo necesites.'
    },
    {
      icon: '💸',
      title: 'Transacciones',
      description: 'Registra cada gasto o ingreso con todos los detalles que necesites.',
      details: [
        'Elige tipo: Gasto 🔴, Ingreso 🟢 o Traspaso 🔄',
        'Asigna una categoría (alimentación, transporte, ocio…)',
        'Añade una descripción y la fecha exacta',
        'Filtra y busca por cualquier campo en el historial'
      ],
      tip: 'Pulsa el botón "+" del dashboard para crear una transacción rápida.'
    },
    {
      icon: '🔄',
      title: 'Transacciones recurrentes',
      description: 'Para gastos o ingresos que se repiten: alquiler, sueldo, suscripciones…',
      details: [
        'Define la frecuencia: diaria, semanal, mensual o anual',
        'La aplicación genera automáticamente cada nueva ocurrencia el día que le toca sin tener que hacer nada',
        'Puedes activar o pausar cualquier movimiento recurrente cuando quieras',
        'Accede a ellas desde la sección "Recurrentes" del menú'
      ],
      tip: 'Una transacción recurrente activa no elimina sus transacciones ya generadas si la pausas.'
    },
    {
      icon: '👥',
      title: 'Gastos compartidos',
      description: 'Cuando un gasto lo pagas tú pero es compartido con varias personas, marca quién te debe parte.',
      details: [
        'En el formulario de transacción, marca la opción "Compartido"',
        'Indica el nombre de la persona que comparte el gasto contigo',
        'SmartSpend calcula automáticamente cuánto te debe cada uno o puedes introducir la cantidad exacta que te debe cada persona',
        'La deuda queda registrada, pudiendo saldarla cuando se pague en efectivo o por transferencia',
        'Estos gastos que no te corresponden a ti, no afectan a tus estadísticas de gasto ni a tu saldo, solo te afectará tu parte del gasto'
      ],
        },
    {
      icon: '⚖️',
      title: 'Deudas y compensaciones',
      description: 'Gestiona quién le debe a quién y compensa automáticamente deudas cruzadas.',
      details: [
        'En el dashboard verás un resumen de deudas pendientes',
        'Cuando saldes una deuda, se generará automáticamente una transacción de compensación en tu cuenta que lo refleje',
      ],
      tip: '¡La compensación automática evita tener que hacer múltiples transferencias!'
    }
  ];

  get isLastStep(): boolean {
    return this.currentStep === this.steps.length - 1;
  }

  get progressPercent(): number {
    return Math.round(((this.currentStep + 1) / this.steps.length) * 100);
  }

  next(): void {
    if (!this.isLastStep) {
      this.currentStep++;
    }
  }

  prev(): void {
    if (this.currentStep > 0) {
      this.currentStep--;
    }
  }

  goToStep(index: number): void {
    this.currentStep = index;
  }

  finish(): void {
    localStorage.setItem(ONBOARDING_KEY, 'true');
    this.closed.emit();
  }

  skip(): void {
    localStorage.setItem(ONBOARDING_KEY, 'true');
    this.closed.emit();
  }

  static isCompleted(): boolean {
    return localStorage.getItem(ONBOARDING_KEY) === 'true';
  }

  static reset(): void {
    localStorage.removeItem(ONBOARDING_KEY);
  }
}
