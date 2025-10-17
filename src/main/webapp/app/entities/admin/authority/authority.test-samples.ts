import { IAuthority, NewAuthority } from './authority.model';

export const sampleWithRequiredData: IAuthority = {
  name: '5284dae9-5f82-493d-b027-bd0ef2d33832',
};

export const sampleWithPartialData: IAuthority = {
  name: '1760dc59-55c6-44c1-80c2-f3ce146bf664',
};

export const sampleWithFullData: IAuthority = {
  name: 'c400b7f2-0684-40a0-bb91-fb69e75db0da',
};

export const sampleWithNewData: NewAuthority = {
  name: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
