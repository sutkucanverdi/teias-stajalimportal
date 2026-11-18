import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Bildirimler } from './shared/bildirimler';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Bildirimler],
  template: `
    <router-outlet />
    <app-bildirimler />
  `,
})
export class App {}
