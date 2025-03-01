import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { IPayment } from '../payment.model';

@Component({
  selector: 'jhi-payment-detail',
  templateUrl: './payment-detail.component.html',
  imports: [SharedModule, RouterModule, FormatMediumDatetimePipe],
})
export class PaymentDetailComponent {
  payment = input<IPayment | null>(null);

  previousState(): void {
    window.history.back();
  }
}
