import dayjs from 'dayjs/esm';

import { IChatRoom, NewChatRoom } from './chat-room.model';

export const sampleWithRequiredData: IChatRoom = {
  id: 20858,
  name: 'defiantly doubtfully',
  createdAt: dayjs('2025-10-17T10:20'),
};

export const sampleWithPartialData: IChatRoom = {
  id: 11114,
  name: 'whether than breastplate',
  createdAt: dayjs('2025-10-17T03:42'),
};

export const sampleWithFullData: IChatRoom = {
  id: 23039,
  name: 'rosy successfully',
  createdAt: dayjs('2025-10-16T22:08'),
};

export const sampleWithNewData: NewChatRoom = {
  name: 'yuck crowded',
  createdAt: dayjs('2025-10-17T14:02'),
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
