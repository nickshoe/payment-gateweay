import dayjs from 'dayjs/esm';
import { PaymentStatus } from 'app/entities/enumerations/payment-status.model';
import { PaymentResult } from 'app/entities/enumerations/payment-result.model';

export interface IPayment {
  id: number;
  externalId?: string | null;
  transactionId?: string | null;
  creditCardNumber?: string | null;
  creditCardHolder?: string | null;
  creditCardSecurityCode?: string | null;
  amount?: number | null;
  createdAt?: dayjs.Dayjs | null;
  updatedAt?: dayjs.Dayjs | null;
  status?: keyof typeof PaymentStatus | null;
  result?: keyof typeof PaymentResult | null;
}

export type NewPayment = Omit<IPayment, 'id'> & { id: null };
