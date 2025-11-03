import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { IUser } from 'app/entities/user/user.model';
import { UserService } from 'app/entities/user/service/user.service';
import { ChatRoomService } from '../service/chat-room.service';
import { IChatRoom } from '../chat-room.model';
import { ChatRoomFormService } from './chat-room-form.service';

import { ChatRoomUpdateComponent } from './chat-room-update.component';

describe('ChatRoom Management Update Component', () => {
  let comp: ChatRoomUpdateComponent;
  let fixture: ComponentFixture<ChatRoomUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let chatRoomFormService: ChatRoomFormService;
  let chatRoomService: ChatRoomService;
  let userService: UserService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ChatRoomUpdateComponent],
      providers: [
        provideHttpClient(),
        FormBuilder,
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    })
      .overrideTemplate(ChatRoomUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(ChatRoomUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    chatRoomFormService = TestBed.inject(ChatRoomFormService);
    chatRoomService = TestBed.inject(ChatRoomService);
    userService = TestBed.inject(UserService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should call User query and add missing value', () => {
      const chatRoom: IChatRoom = { id: 456 };
      const members: IUser[] = [{ id: 12027 }];
      chatRoom.members = members;

      const userCollection: IUser[] = [{ id: 14865 }];
      jest.spyOn(userService, 'query').mockReturnValue(of(new HttpResponse({ body: userCollection })));
      const additionalUsers = [...members];
      const expectedCollection: IUser[] = [...additionalUsers, ...userCollection];
      jest.spyOn(userService, 'addUserToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ chatRoom });
      comp.ngOnInit();

      expect(userService.query).toHaveBeenCalled();
      expect(userService.addUserToCollectionIfMissing).toHaveBeenCalledWith(
        userCollection,
        ...additionalUsers.map(expect.objectContaining),
      );
      expect(comp.usersSharedCollection).toEqual(expectedCollection);
    });

    it('Should update editForm', () => {
      const chatRoom: IChatRoom = { id: 456 };
      const members: IUser = { id: 3311 };
      chatRoom.members = [members];

      activatedRoute.data = of({ chatRoom });
      comp.ngOnInit();

      expect(comp.usersSharedCollection).toContain(members);
      expect(comp.chatRoom).toEqual(chatRoom);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IChatRoom>>();
      const chatRoom = { id: 123 };
      jest.spyOn(chatRoomFormService, 'getChatRoom').mockReturnValue(chatRoom);
      jest.spyOn(chatRoomService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ chatRoom });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: chatRoom }));
      saveSubject.complete();

      // THEN
      expect(chatRoomFormService.getChatRoom).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(chatRoomService.update).toHaveBeenCalledWith(expect.objectContaining(chatRoom));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IChatRoom>>();
      const chatRoom = { id: 123 };
      jest.spyOn(chatRoomFormService, 'getChatRoom').mockReturnValue({ id: null });
      jest.spyOn(chatRoomService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ chatRoom: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: chatRoom }));
      saveSubject.complete();

      // THEN
      expect(chatRoomFormService.getChatRoom).toHaveBeenCalled();
      expect(chatRoomService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IChatRoom>>();
      const chatRoom = { id: 123 };
      jest.spyOn(chatRoomService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ chatRoom });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(chatRoomService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareUser', () => {
      it('Should forward to userService', () => {
        const entity = { id: 123 };
        const entity2 = { id: 456 };
        jest.spyOn(userService, 'compareUser');
        comp.compareUser(entity, entity2);
        expect(userService.compareUser).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
