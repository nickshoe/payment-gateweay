import dayjs from 'dayjs/esm';

import { IPayment, NewPayment } from './payment.model';

export const sampleWithRequiredData: IPayment = {
  id: 4942,
  externalId: 'on huddle',
  transactionId: 'after',
  creditCardNumber: 'guilt shudder',
  creditCardHolder: 'besides',
  creditCardSecurityCode: 'oblong sharply fiddle',
  amount: 1158.25,
};

export const sampleWithPartialData: IPayment = {
  id: 11071,
  externalId: 'roughly boo abaft',
  transactionId: 'redact hunt',
  creditCardNumber: 'oof throbbing astride',
  creditCardHolder: 'against soliloquy mythology',
  creditCardSecurityCode: 'troubled rag ack',
  amount: 3156.13,
  updatedAt: dayjs('2025-02-28T09:23'),
};

export const sampleWithFullData: IPayment = {
  id: 28239,
  externalId: 'hourly unwieldy',
  transactionId: 'because',
  creditCardNumber: 'eke',
  creditCardHolder: 'plastic',
  creditCardSecurityCode: 'as connect',
  amount: 20521.33,
  createdAt: dayjs('2025-02-27T18:24'),
  updatedAt: dayjs('2025-02-27T17:45'),
  status: 'RUNNING',
  result: 'FAILURE',
};

export const sampleWithNewData: NewPayment = {
  externalId: 'within',
  transactionId: 'hmph',
  creditCardNumber: 'barring celebrated',
  creditCardHolder: 'microblog',
  creditCardSecurityCode: 'aged',
  amount: 20639.29,
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
