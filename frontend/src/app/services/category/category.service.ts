import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/internal/Observable';
import { Category } from '../../interfaces/category.interface';


@Injectable({
  providedIn: 'root'
})
export class CategoryService {

   private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  getCategoriesForType(type: 'INCOME' | 'EXPENSE'): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.apiUrl}/categories?type=${type}`);
  }
}
