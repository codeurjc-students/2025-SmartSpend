import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type Theme = 'dark' | 'light';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly STORAGE_KEY = 'ss-theme';
  private readonly defaultTheme: Theme = 'dark';

  private _theme$ = new BehaviorSubject<Theme>(this.loadTheme());
  readonly theme$ = this._theme$.asObservable();

  get isDark(): boolean {
    return this._theme$.value === 'dark';
  }

  get current(): Theme {
    return this._theme$.value;
  }

  /** Aplica el tema guardado al arrancar la app */
  init(): void {
    this.applyTheme(this._theme$.value);
  }

  toggle(): void {
    const next: Theme = this._theme$.value === 'dark' ? 'light' : 'dark';
    this.setTheme(next);
  }

  private setTheme(theme: Theme): void {
    this._theme$.next(theme);
    localStorage.setItem(this.STORAGE_KEY, theme);
    this.applyTheme(theme);
  }

  private applyTheme(theme: Theme): void {
    const html = document.documentElement;
    if (theme === 'light') {
      html.classList.add('light-theme');
    } else {
      html.classList.remove('light-theme');
    }
  }

  private loadTheme(): Theme {
    const saved = localStorage.getItem(this.STORAGE_KEY) as Theme | null;
    return saved === 'light' ? 'light' : this.defaultTheme;
  }
}
