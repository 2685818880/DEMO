// import { getEnumTypes } from '@/api/enum-types'
// import Label from './src/label'
// import Select from './src/select'
// import * as enumsExtends from '../../components/Battery/extend'
//
// // 枚举插件
// const Enums = {
//   install (Vue, options) {
//     Vue.prototype.$_MyEnums = new Vue({
//       data: {
//         enumTypes: {
//           trayStatus: {}, // 托盘状态
//           batteryStatus: {}, // 电芯状态
//           batteryType: {}, // 电芯类型
//           dispatchStatus: {}, // 工序状态
//           posType: {}, //  位置类型
//           fmCreateMode: {}, //  表单的创建方式枚举
//           fmStatus: {}, //  单据状态枚举
//           palletizeStatus: {}, //  组拆盘状态枚举
//           palletSplitPolicy: {}, //  拆盘策略
//           errorCode: {}, //  异常状态
//           movePolicy: {}, //  拆盘策略
//           taskType: {}, //  任务类型
//           taskStatus: {}, //  任务使用状态
//           taskTriggerType: {}, // 任务触发状态,
//           bizType: {}, // 业务类型
//           planType: {}, // 方案类型,
//           schedulerType: {}, // 计划状态
//           schedulerStatus: {}, // 计划类型,
//           locationForbiddenStatus: {}, // 库位禁用状态
//           storageStatus: {}, // 库存使用状态
//           locationUseStatus: {}, // 库位使用状态
//           locationType: {}, // 库位类型,
//           stockType: {}, // 库存状态
//           timeIntervalUnit: {},
//           excuteResult: {},
//           checkType: {}, // 校验规则类型
//           socketEndpointType: {},
//           trayCarryStatus: {},
//           lockDirection: {},
//           locationLockType: {},
//           lockStatus: {},
//           appointmentStatus: {},
//           createType: {}
//         }
//       },
//       created () {
//         getEnumTypes().then(res => {
//           let data = res.object
//           Object.keys(this.enumTypes).forEach((key) => {
//             const tmp = data[key]
//             const extendProps = enumsExtends[key] || {}
//
//             for (let prop in tmp) {
//               tmp[prop] = {
//                 value: prop,
//                 label: tmp[prop],
//                 ...extendProps[prop]
//               }
//             }
//           })
//           this.enumTypes = Object.assign(this.enumTypes, data)
//         })
//       }
//     })
//
//     Vue.prototype.$EnumTranslate = function (type, key) {
//       if (!type || !key || !this.$_MyEnums.enumTypes[type]) return ''
//
//       return this.$_MyEnums.enumTypes[type]
//         ? this.$_MyEnums.enumTypes[type][key]
//           ? this.$_MyEnums.enumTypes[type][key].label : ''
//         : ''
//     }
//
//     Vue.component(Label.name, Label)
//     Vue.component(Select.name, Select)
//   }
// }
//
// export default Enums
