import dayjs from 'dayjs/esm';

import { IMessage, NewMessage } from './message.model';

export const sampleWithRequiredData: IMessage = {
  id: 26342,
  content: 'how punctually',
  sentAt: dayjs('2025-10-16T15:57'),
};

export const sampleWithPartialData: IMessage = {
  id: 14584,
  content: 'favorite whose intellect',
  sentAt: dayjs('2025-10-16T15:22'),
};

export const sampleWithFullData: IMessage = {
  id: 5673,
  content: 'handle viciously a',
  sentAt: dayjs('2025-10-17T11:23'),
};

export const sampleWithNewData: NewMessage = {
  content: 'fly',
  sentAt: dayjs('2025-10-17T07:46'),
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
