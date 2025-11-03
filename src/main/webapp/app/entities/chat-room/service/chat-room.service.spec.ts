import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { IChatRoom } from '../chat-room.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../chat-room.test-samples';

import { ChatRoomService, RestChatRoom } from './chat-room.service';

const requireRestSample: RestChatRoom = {
  ...sampleWithRequiredData,
  createdAt: sampleWithRequiredData.createdAt?.toJSON(),
};

describe('ChatRoom Service', () => {
  let service: ChatRoomService;
  let httpMock: HttpTestingController;
  let expectedResult: IChatRoom | IChatRoom[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(ChatRoomService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.find(123).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should create a ChatRoom', () => {
      const chatRoom = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(chatRoom).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a ChatRoom', () => {
      const chatRoom = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(chatRoom).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a ChatRoom', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of ChatRoom', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a ChatRoom', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addChatRoomToCollectionIfMissing', () => {
      it('should add a ChatRoom to an empty array', () => {
        const chatRoom: IChatRoom = sampleWithRequiredData;
        expectedResult = service.addChatRoomToCollectionIfMissing([], chatRoom);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(chatRoom);
      });

      it('should not add a ChatRoom to an array that contains it', () => {
        const chatRoom: IChatRoom = sampleWithRequiredData;
        const chatRoomCollection: IChatRoom[] = [
          {
            ...chatRoom,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addChatRoomToCollectionIfMissing(chatRoomCollection, chatRoom);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a ChatRoom to an array that doesn't contain it", () => {
        const chatRoom: IChatRoom = sampleWithRequiredData;
        const chatRoomCollection: IChatRoom[] = [sampleWithPartialData];
        expectedResult = service.addChatRoomToCollectionIfMissing(chatRoomCollection, chatRoom);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(chatRoom);
      });

      it('should add only unique ChatRoom to an array', () => {
        const chatRoomArray: IChatRoom[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const chatRoomCollection: IChatRoom[] = [sampleWithRequiredData];
        expectedResult = service.addChatRoomToCollectionIfMissing(chatRoomCollection, ...chatRoomArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const chatRoom: IChatRoom = sampleWithRequiredData;
        const chatRoom2: IChatRoom = sampleWithPartialData;
        expectedResult = service.addChatRoomToCollectionIfMissing([], chatRoom, chatRoom2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(chatRoom);
        expect(expectedResult).toContain(chatRoom2);
      });

      it('should accept null and undefined values', () => {
        const chatRoom: IChatRoom = sampleWithRequiredData;
        expectedResult = service.addChatRoomToCollectionIfMissing([], null, chatRoom, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(chatRoom);
      });

      it('should return initial array if no ChatRoom is added', () => {
        const chatRoomCollection: IChatRoom[] = [sampleWithRequiredData];
        expectedResult = service.addChatRoomToCollectionIfMissing(chatRoomCollection, undefined, null);
        expect(expectedResult).toEqual(chatRoomCollection);
      });
    });

    describe('compareChatRoom', () => {
      it('Should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareChatRoom(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('Should return false if one entity is null', () => {
        const entity1 = { id: 123 };
        const entity2 = null;

        const compareResult1 = service.compareChatRoom(entity1, entity2);
        const compareResult2 = service.compareChatRoom(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey differs', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 456 };

        const compareResult1 = service.compareChatRoom(entity1, entity2);
        const compareResult2 = service.compareChatRoom(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey matches', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 123 };

        const compareResult1 = service.compareChatRoom(entity1, entity2);
        const compareResult2 = service.compareChatRoom(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
