import formats from './utils/format'
import { tagRender } from './renders/tagRender'
import { tipRender } from './renders/tipRender'
import { defaultRender } from './renders/defaultRender'
import { datetimeRender } from './renders/datetimeRender'
import { cutoffRender } from './renders/cutoffRender'
import { percentRender } from './renders/percentRender'

export const formatMap = {
  datetime: {
    format: formats.formatDatetime,
    render: datetimeRender
  },
  money: {
    format: formats.formatMoney,
    render: defaultRender
  },
  tag: {
    format: formats.formatTag,
    render: tagRender
  },
  tip: {
    format: formats.defaultFormat,
    render: tipRender
  },
  cutoff: {
    format: formats.defaultFormat,
    render: cutoffRender
  },
  percent: {
    format: formats.formatPercent,
    render: percentRender
  },
  default: {
    format: formats.defaultFormat,
    render: defaultRender
  }
}
