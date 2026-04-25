export const FormStatus = {
  CreatedAndExecuting: '创建&执行',
  Created: '已创建',
  Picked: '待拣选',
  Waiting: '待出库',
  Executing: '作业中',
  Finished: '待审核',
  Closed: '已关闭',
  Exception: '异常',
  Invalid: '作废'
}

export const AdjustStatus = {
  Created: '待审核',
  Closed: '已关闭',
  Cancelled: '已作废'

}

export const ContainerStatus = {
  haveGoods: '有货',
  empty: '无货'
}

export const JobType = {
  StorageIn: '入库',
  StorageOut: '出库'
}

export const OpeLevel = {
  system_level: '系统级别',
  customize_level: '自定义级别'
}

export const workstationModeType = {
  inbound: '入库',
  outbound: '出库',
  tally: '理货',
  no_mode: '无模式',
  takestock: '盘点'
}

export const LocationForbiddenStatus = {
  AllowInAndOut: '可进出',
  ForbiddenAll: '不可进出',
  ForbiddenInOnly: '不可进',
  ForbiddenOutOnly: '不可出'
}

export const LockStatus = {
  inLock: '锁入',
  outLock: '锁出',
  inAndOutLock: '锁出入'
}

export const LockType = {
  artificial: '人工锁',
  business: '业务锁',
  doubleLock: '双向锁'
}

export const StorageStatus = {
  haveGoods: '有货',
  empty: '无货'
}

export const StorageType = {
  cubic: '立库库位',
  virtual: '虚拟库位',
  horizontal: '平库库位'
}

export const MaterialStatus = {
  Q: '合格',
  N: '不合格',
  W: '待检',
  E: '免检'
}

export const InventoryType = {
  REAL_TIME_INVENTORY: '即时盘点',
  CIRCLE_INVENTORY: '循环盘点',
  YEAR_END_INVENTORY: '年终盘点'
}

// 盘点盈亏单据审核状态
export const AuditStatus = {
  Created: '创建',
  Finished: '已审核',
  Exception: '审核异常'
}

// 盘点盈亏单类型
export const InventoryProfitLossType = {
  PROFIT_RESULT: '盘盈',
  LOSS_RESULT: '盘亏',
  EQUAL_RESULT: '持平'
}

// 盘点提交状态类型
export const SubmitStatus = {
  Created: '就绪',
  submitting: '部分提交',
  Finished: '已提交',
  ERP_Checked: 'ERP已过账',
  Exception: '接口异常'
}

// 盘点质量状态
export const InventoryMaterialQuality = {
  Q: '合格',
  N: '不合格',
  W: '待检',
  S: '冻结',
  T: '暂存',
  U: '质检中',
  R: '重检审批中'
}

export const OutboundType = {
  delivery: '叫料出库',
  forced: '强制出库'
}

export const MoveType = {
  TRUTH: '实盘',
  VIRTUAL: '过账'
}

export const QualityType = {
  PURCHASE_QUALITY: '采购质检',
  OVERDUE_RECHECK_QUALITY: '过期重检',
  INVENTORY_QUALITY: '库存报检'
}

export const QualityStatus = {
  Q: '合格',
  N: '不合格',
  W: '待检',
  E: '免检'
}

export const TaskStatus = {
  Created: '等待',
  Waiting: '待下发',
  Send: '已下发',
  Executing: '执行中',
  Finished: '执行完成',
  Error: '执行异常',
  Destroyed: '作废',
  HandUp: '挂起',
  Empty: '空出',
  Full: '满入'
}

export const TaskType = {
  Stock_In: '上架',
  Stock_Out: '下架',
  Stock_Move: '摆渡',
  Stock_Change: '移库',
  Transport: '移动',
  Stock_Weak_Check: '忽视校验',
  Empty_Move: '空托移动',
  Empty_Call_Move: '空托呼叫移动',
  Inventory_Check: '盘点',
  Pick: '拣选',
  agvTask: 'agv'
}

export const AllocationStatus = {
  1: '未抽检',
  2: '已抽检'
}

export const AllocationSamplingStatus = {
  1: '未取样',
  2: '已取样'
}

export const YesOrNo = {
  Y: '是',
  N: '否'
}

export const YesOrNoB = {
  true: '是',
  false: '否'
}

export const PalletizeFormStatus = {
  CreatedAndExecuting: '创建&执行',
  Created: '已创建',
  Executing: '入库中',
  Finished: '已结束',
  Closed: '关闭',
  Exception: '异常',
  Invalid: '作废'
}

export const OutboundPalletFormStatus = {
  CreatedAndExecuting: '创建&执行',
  Created: '已创建',
  Waiting: '待下发任务',
  Executing: '作业中',
  Finished: '已结束',
  Closed: '关闭',
  Exception: '异常',
  Invalid: '作废'
}

export const statisticsType = {
  inbound: '组盘入库',
  outbound: '叫料出库',
  applyEmpty: '呼叫空托'
}

export const SummaryType = {
  Monthly: '按月统计',
  Daily: '按日统计'
}

export const TakeStockType = {
  REAL_TIME: '即时盘点',
  CIRCLE: '循环盘点',
  YEAR_END: '年终盘点'
}

export const StockItemStatus = {
  Created: '已创建',
  OutExecuting: '出库中',
  OutFinished: '出库完成',
  BackExecuting: '回库中',
  WaitAudit: '待审核',
  Closed: '已关闭',
  Exception: '异常',
  Cancelled: '作废'
}

export const FormLevel = {
  system_level: '系统级别',
  customize_level: '用户级别'
}

export const InventoryStatus = {
  C: '初始',
  P: '优先',
  A: '正常',
  R: '退货',
  B: '备货中',
  T: '暂存',
  Q: '质检中',
  I: '盘点中',
  X: '差异锁定中'
}

export const BatchStatus = {
  A: '优先',
  B: '使用中',
  C: '退料',
  D: '正常',
  S: '冻结'
}

export const ProfitLossType = {
  PROFIT: '盈',
  LOSS: '亏',
  FLAT: '持平'
}

// 扩展字段类型
export const ExtraFieldType = {
  PalletizeForm: '组盘单',
  Asn: '收料单',
  AsnItem: '收料单明细',
  AsnDetail: '收料单详情',
  Order: '需求单',
  OrderItem: '需求明细',
  OrderDetail: '需求详情'
}

// 组件名对应的扩展字段组
export const ExtraFieldGroup = {
  PalletizeList: 'PalletizeForm',
  PalletizeItem: 'PalletizeDetailModal',
  OutboundList: 'Order',
  OutboundItem: 'OrderItem',
  OutboundDetail: 'OrderDetail',
  AsnList: 'Asn',
  AsnItem: 'AsnItem'
}

export const OutFormType = {
  scrapOut: '报废出库',
  simpleOut: '常规出库',
  out_cost: '成本中心出库',
  out_delivery: '外向交货',
  out_source: '委外出库',
  out_purchase_return: '采购退货',
  out_Trans: '调拨出库',
  out_reversal: '冲销出库',
  out_sales: '销售出库',
  CallOutBySku: '按SKU出库',
  CallOutByType: '按托盘类型出库',
  ExitOut: '退料出库'
}

export const AllotStatus = {
  CreatedAndExecuting: '创建&执行',
  Created: '已创建',
  Allotting: '部分分配',
  Allotted: '分配完成'
}

// 包装方式
export const Zbzfs = {
  0: '标准包',
  1: '分包-子',
  2: '合包-子',
  3: '分包-母',
  4: '合包-母'
}

// 条码状态
export const Ztmzt = {
  0: '初始',
  1: '非限制库存',
  2: '备货',
  3: '销售出库',
  7: '退货',
  8: '调拨出库',
  9: '质检',
  A: '报废',
  X: '已删除',
  R: '生产入库'
}

// 包装类型
export const PackageType = {
  NORMAL: '标准',
  SPLIT: '分包',
  MERGE: '合包'
}

// 提交状态
export const OnepiecceSubmitStatus = {
  NoFinished: '未提供成功',
  Created: '就绪',
  Finished: '已提交',
  Exception: '接口异常'
}

// 收料状态
export const ReceiptFlag = {
  true: '已收料',
  false: '未收料'
}

// 分包等级
export const PackageLevel = {
  NORMAL: '标准包',
  SPLIT_ITEM: '分包子条码',
  SPLIT_MAIN: '分包母条码',
  MERGE_ITEM: '合包子条码',
  MERGE_MAIN: '合包母条码'
}

export const WaveType = {
  B2B: 'B2B',
  B2C: 'B2C',
  Normal: '普通合单'
}

export const WaveStatus = {
  CreatedAndExecuting: '创建&执行',
  create: '新建',
  cancel: '取消',
  allotting: '部分分配',
  allotted: '全部分配',
  claim: '认领',
  picking: '部分拣货',
  picked: '拣货完成',
  re_checking: '部分复核',
  re_checked: '全部复核',
  transiting: '部分发运',
  transited: '全部发运',
  closed: '关闭'
}

export const OperateStatus = {
  '订单分配': '订单分配',
  '取消分配': '取消分配',
  '复核中': '复核中',
  '复核完成': '复核完成',
  '生成拣货任务': '生成拣货任务',
  '取消拣货任务': '取消拣货任务',
  '关闭中': '关闭中',
  '发送关闭失败': '发送关闭失败',
  '关闭完成': '关闭完成'
}

export const FlagStatus = {
  Y: '是',
  N: '否'
}

// 拣选状态
export const PickStatus = {
  createdAndExecuting: '创建&执行',
  create: '新建',
  // execute: '分配执行',
  executing: '执行中',
  // wait_picking: '等待拣选',
  picking: '部分拣货',
  picked: '拣货完成',
  // Sowed: '播种完成',
  // re_checking: '部分复核',
  // re_checked: '全部复核',
  closed: '关闭'
}

// 拣选类型
export const PickType = {
  wave_pick: '波次拣选',
  order_pick: '订单拣选'
}

export const LoadingStatus = {
  CreatedAndExecuting: '创建&执行',
  create: '新建',
  loading: '发运中',
  confirmed: '装车确认',
  posted: '完成过账',
  close: '关闭'
}

export const LoadingDetailStatus = {
  CreatedAndExecuting: '创建&执行',
  create: '新建',
  loading: '发运中',
  loaded: '待装车',
  confirmed: '装车确认',
  posted: '完成过账',
  returned: '退回'
}

export const Remark = {
  'ZA_国内-典型': 'ZA_国内-典型',
  'XA_海外': 'XA_海外',
  'ZB_国内-欧派': 'ZB_国内-欧派',
  'ZC_国内-酒店': 'ZC_国内-酒店',
  'ZD_国内-崔佧': 'ZD_国内-崔佧',
  'ZE_国内-科施德': 'ZE_国内-科施德',
  'ZF_国内-沙发': 'ZF_国内-沙发'
}

export const TransportType = {
  '01': '陆运物流',
  '02': '海运物流',
  '03': '客户自提',
  '04': '厂家配送',
  '05': '补单过账'
}
