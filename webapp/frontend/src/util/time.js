
import moment from 'moment'

export function option2time(o) {
  switch (o) {
    case 'LAST_HOUR': return moment().subtract(1, 'hour').unix() * 1000
    case 'LAST_THREE_HOURS': return moment().subtract(3, 'hours').unix() * 1000
    case 'LAST_24_HOURS': return moment().subtract(24, 'hours').unix() * 1000
    case 'TODAY': return moment().startOf('day').unix() * 1000
    case 'YESTERDAY': return moment().subtract(1, 'days').startOf('day').unix() * 1000
    case 'THIS_WEEK': return moment().startOf('week').unix() * 1000
    case 'NOW': return moment().unix() * 1000
  }
  return moment().subtract(1, 'day').unix() * 1000
}

export function time2str(t) {
  if (!t) {
    return ''
  }
  return moment(t).format('MMMM D hh:mm:ss a')
}
