import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IPayment, NewPayment } from '../payment.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IPayment for edit and NewPaymentFormGroupInput for create.
 */
type PaymentFormGroupInput = IPayment | PartialWithRequiredKeyOf<NewPayment>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IPayment | NewPayment> = Omit<T, 'createdAt' | 'updatedAt'> & {
  createdAt?: string | null;
  updatedAt?: string | null;
};

type PaymentFormRawValue = FormValueOf<IPayment>;

type NewPaymentFormRawValue = FormValueOf<NewPayment>;

type PaymentFormDefaults = Pick<NewPayment, 'id' | 'createdAt' | 'updatedAt'>;

type PaymentFormGroupContent = {
  id: FormControl<PaymentFormRawValue['id'] | NewPayment['id']>;
  externalId: FormControl<PaymentFormRawValue['externalId']>;
  transactionId: FormControl<PaymentFormRawValue['transactionId']>;
  creditCardNumber: FormControl<PaymentFormRawValue['creditCardNumber']>;
  creditCardHolder: FormControl<PaymentFormRawValue['creditCardHolder']>;
  creditCardSecurityCode: FormControl<PaymentFormRawValue['creditCardSecurityCode']>;
  amount: FormControl<PaymentFormRawValue['amount']>;
  createdAt: FormControl<PaymentFormRawValue['createdAt']>;
  updatedAt: FormControl<PaymentFormRawValue['updatedAt']>;
  status: FormControl<PaymentFormRawValue['status']>;
  result: FormControl<PaymentFormRawValue['result']>;
};

export type PaymentFormGroup = FormGroup<PaymentFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class PaymentFormService {
  createPaymentFormGroup(payment: PaymentFormGroupInput = { id: null }): PaymentFormGroup {
    const paymentRawValue = this.convertPaymentToPaymentRawValue({
      ...this.getFormDefaults(),
      ...payment,
    });
    return new FormGroup<PaymentFormGroupContent>({
      id: new FormControl(
        { value: paymentRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      externalId: new FormControl(paymentRawValue.externalId, {
        validators: [Validators.required],
      }),
      transactionId: new FormControl(paymentRawValue.transactionId, {
        validators: [Validators.required],
      }),
      creditCardNumber: new FormControl(paymentRawValue.creditCardNumber, {
        validators: [Validators.required],
      }),
      creditCardHolder: new FormControl(paymentRawValue.creditCardHolder, {
        validators: [Validators.required],
      }),
      creditCardSecurityCode: new FormControl(paymentRawValue.creditCardSecurityCode, {
        validators: [Validators.required],
      }),
      amount: new FormControl(paymentRawValue.amount, {
        validators: [Validators.required],
      }),
      createdAt: new FormControl(paymentRawValue.createdAt),
      updatedAt: new FormControl(paymentRawValue.updatedAt),
      status: new FormControl(paymentRawValue.status),
      result: new FormControl(paymentRawValue.result),
    });
  }

  getPayment(form: PaymentFormGroup): IPayment | NewPayment {
    return this.convertPaymentRawValueToPayment(form.getRawValue() as PaymentFormRawValue | NewPaymentFormRawValue);
  }

  resetForm(form: PaymentFormGroup, payment: PaymentFormGroupInput): void {
    const paymentRawValue = this.convertPaymentToPaymentRawValue({ ...this.getFormDefaults(), ...payment });
    form.reset(
      {
        ...paymentRawValue,
        id: { value: paymentRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): PaymentFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      createdAt: currentTime,
      updatedAt: currentTime,
    };
  }

  private convertPaymentRawValueToPayment(rawPayment: PaymentFormRawValue | NewPaymentFormRawValue): IPayment | NewPayment {
    return {
      ...rawPayment,
      createdAt: dayjs(rawPayment.createdAt, DATE_TIME_FORMAT),
      updatedAt: dayjs(rawPayment.updatedAt, DATE_TIME_FORMAT),
    };
  }

  private convertPaymentToPaymentRawValue(
    payment: IPayment | (Partial<NewPayment> & PaymentFormDefaults),
  ): PaymentFormRawValue | PartialWithRequiredKeyOf<NewPaymentFormRawValue> {
    return {
      ...payment,
      createdAt: payment.createdAt ? payment.createdAt.format(DATE_TIME_FORMAT) : undefined,
      updatedAt: payment.updatedAt ? payment.updatedAt.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
