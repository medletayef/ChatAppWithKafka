import { IUser } from './user.model';

export const sampleWithRequiredData: IUser = {
  id: 5631,
  login: '0V1D5p',
};

export const sampleWithPartialData: IUser = {
  id: 3021,
  login: '8@VVmz\\-3\\Jx1rE',
};

export const sampleWithFullData: IUser = {
  id: 31666,
  login: 'y2',
};
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
