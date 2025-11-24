import { Pipe, PipeTransform } from '@angular/core';

import dayjs from 'dayjs/esm';

@Pipe({
  standalone: true,
  name: 'formatTimestamp',
})
export default class TimestampPipe implements PipeTransform {
  transform(date: any): string {
    date = dayjs(date);
    if (date.isSame(dayjs(), 'day')) {
      return date.format('HH:mm');
    } else {
      return date.format('DD-MM-YYYY HH:mm');
    }
  }
}
