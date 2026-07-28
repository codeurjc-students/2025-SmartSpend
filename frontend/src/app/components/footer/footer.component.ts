import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { VersionService } from '../../services/version/version.service';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.css'
})
export class FooterComponent implements OnInit {
  version: string = 'loading...';

  constructor(private versionService: VersionService) {}

  ngOnInit(): void {
    this.versionService.getVersion().subscribe({
      next: (response) => {
        this.version = response.version;
      },
      error: () => {
        this.version = 'unavailable';
      }
    });
  }
}
