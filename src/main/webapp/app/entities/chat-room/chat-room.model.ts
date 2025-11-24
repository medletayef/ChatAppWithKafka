import dayjs from 'dayjs/esm';
import { IUser } from 'app/entities/user/user.model';

export interface IChatRoom {
  id: number;
  name?: string | null;
  createdBy?: string | null;
  createdDate?: dayjs.Dayjs | null;
  members?: Pick<IUser, 'id' | 'login'>[] | null;
}

export type NewChatRoom = Omit<IChatRoom, 'id'> & { id: null; members: string[] };
