import errSound from '@/assets/sounds/bell.ogg'

const ERROR = new Audio(errSound)

export function error() {
  ERROR.play()
}
