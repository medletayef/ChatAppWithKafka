import { IChatRoom } from '../../chat-room/chat-room.model';
import { IUser } from '../../user/user.model';
import { InvitationStatus } from '../../../enum/InvitationStatus';
import dayjs from 'dayjs';
export interface IInvitation {
  id: number;
  status?: InvitationStatus | null;
  chatRoom?: Pick<IChatRoom, 'id' | 'name'> | null;
  user?: Pick<IUser, 'id' | 'login' | 'fullName'> | null;
  // createdAt, createdBy from AbstractAuditingEntity inherited
  createdDate?: dayjs.Dayjs | null;
  createdBy?: string | null;
}

export type NewInvitation = Omit<IInvitation, 'id'> & { id: null };
