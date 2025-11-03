import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../chat-room.test-samples';

import { ChatRoomFormService } from './chat-room-form.service';

describe('ChatRoom Form Service', () => {
  let service: ChatRoomFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ChatRoomFormService);
  });

  describe('Service methods', () => {
    describe('createChatRoomFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createChatRoomFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
            createdAt: expect.any(Object),
            members: expect.any(Object),
          }),
        );
      });

      it('passing IChatRoom should create a new form with FormGroup', () => {
        const formGroup = service.createChatRoomFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
            createdAt: expect.any(Object),
            members: expect.any(Object),
          }),
        );
      });
    });

    describe('getChatRoom', () => {
      it('should return NewChatRoom for default ChatRoom initial value', () => {
        const formGroup = service.createChatRoomFormGroup(sampleWithNewData);

        const chatRoom = service.getChatRoom(formGroup) as any;

        expect(chatRoom).toMatchObject(sampleWithNewData);
      });

      it('should return NewChatRoom for empty ChatRoom initial value', () => {
        const formGroup = service.createChatRoomFormGroup();

        const chatRoom = service.getChatRoom(formGroup) as any;

        expect(chatRoom).toMatchObject({});
      });

      it('should return IChatRoom', () => {
        const formGroup = service.createChatRoomFormGroup(sampleWithRequiredData);

        const chatRoom = service.getChatRoom(formGroup) as any;

        expect(chatRoom).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IChatRoom should not enable id FormControl', () => {
        const formGroup = service.createChatRoomFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewChatRoom should disable id FormControl', () => {
        const formGroup = service.createChatRoomFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
