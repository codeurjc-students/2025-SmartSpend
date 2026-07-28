import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

interface VersionResponse {
  version: string;
}

@Injectable({
  providedIn: 'root'
})
export class VersionService {
  constructor(private http: HttpClient) {}

  getVersion(): Observable<VersionResponse> {
    return this.http.get<VersionResponse>('/api/v1/version').pipe(
      catchError(() => of({ version: 'unknown' }))
    );
  }
}
