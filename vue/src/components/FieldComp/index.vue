<template>
  <component
    :is="compName"
    v-bind="{...$attrs, ...compProps}"
    v-on="$listeners"
  />
</template>

<script>
  import EnumSelect from '@comp/EnumSelect/index'
  const dataToComp = {
    'label': {
      compName: 'biz-span'
    },
    'string': {
      compName: 'el-input'
    },
    'enum': {
      compName: 'enum-select'
    },
    'dateRange': {
      compName: 'el-date-picker',
      props: {
        type: 'daterange',
        'range-separator': '至',
        'start-placeholder': '开始日期',
        'end-placeholder': '结束日期'
      }
    }
  }

  export default {
    name: 'FieldComp',

    components: {
      EnumSelect,
      BizSpan: {
        props: {
          value: {
            type: String
          }
        },
        render() {
          return (
            <span>
              { this.value }
            </span>
          )
        }
      }
    },

    props: {
      type: {
        default: 'string',
        validator(val) {
          if (Object.keys(dataToComp).indexOf(val) === -1) {
            return false
          }
          return true
        }
      }
    },

    computed: {
      compName() {
        return dataToComp[this.type].compName
      },
      compProps() {
        return dataToComp[this.type].props
      }
    }
  }
</script>
