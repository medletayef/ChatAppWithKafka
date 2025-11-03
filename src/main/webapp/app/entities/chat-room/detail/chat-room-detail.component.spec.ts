import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { ChatRoomDetailComponent } from './chat-room-detail.component';

describe('ChatRoom Management Detail Component', () => {
  let comp: ChatRoomDetailComponent;
  let fixture: ComponentFixture<ChatRoomDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChatRoomDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./chat-room-detail.component').then(m => m.ChatRoomDetailComponent),
              resolve: { chatRoom: () => of({ id: 123 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(ChatRoomDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ChatRoomDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('Should load chatRoom on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', ChatRoomDetailComponent);

      // THEN
      expect(instance.chatRoom()).toEqual(expect.objectContaining({ id: 123 }));
    });
  });

  describe('PreviousState', () => {
    it('Should navigate to previous state', () => {
      jest.spyOn(window.history, 'back');
      comp.previousState();
      expect(window.history.back).toHaveBeenCalled();
    });
  });
});
