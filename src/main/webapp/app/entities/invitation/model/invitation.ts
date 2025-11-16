import { IChatRoom } from '../../chat-room/chat-room.model';
import { IUser } from '../../user/user.model';
import { InvitationStatus } from '../../../enum/InvitationStatus';
import dayjs from 'dayjs';
export interface IInvitation {
  id: number;
  status?: InvitationStatus | null;
  chatRoom?: Pick<IChatRoom, 'id' | 'name'> | null;
  user?: Pick<IUser, 'id' | 'login'> | null;
  createdDate?: dayjs.Dayjs | null;
  createdBy?: string | null;
}
