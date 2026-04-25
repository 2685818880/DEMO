
const generalConfig = {
  palletize: {
    pageTitle: "人工组盘",
    formProps: {
      fields: [
        {
          label: '仓库',
          prop: 'house_name',
          key: 'houseCode',
          fieldType: 'picker',
          props: {
            init: 'warehouse',
            placeholder: '请选择仓库',
            labelName: 'house_name',
            valueName: 'house_no'
          },
          onChange: ['reloadPicker-device_name-house_code:houseCode']
        },
        {
          label: '入库口',
          prop: 'device_name',
          key: 'deviceCode',
          fieldType: 'picker',
          props: {
            init: 'device',
            extraParams: {
              device_type: 'Entrance'
            },
            placeholder: '请选择入库口',
            labelName: 'label',
            valueName: 'value'
          }
        }
      ]
    },
    listProps: {
      columns: [
        {
          label: '物料SN',
          prop: 'serial_no'
        },
        {
          label: '物料编码',
          prop: 'sku_code'
        },
        {
          label: '物料名称',
          prop: 'sku_name'
        },
        {
          label: '数量',
          prop: 'primary_qty'
        },
        {
          label: '单位',
          prop: 'primary_unit'
        }
      ]
    },
    buttonProps: {

    },
    submitProps: {

    },
    completeProps: {}
  }
}
