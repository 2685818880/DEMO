<template>
  <el-popover
    :popper-options="{ boundariesElement: 'body', gpuAcceleration: false }"
    :placement="placement"
    width="700"
    :trigger="trigger">
    <form-view v-bind="$attrs" v-on="$listeners"></form-view>
    <slot name="reference" slot="reference">
      <el-button type="primary" icon="el-icon-info">{{ title }}</el-button>
    </slot>
  </el-popover>
</template>

<script>
  import FormView from '@comp/FormView'

  export default {
    name: 'PopoverFormView',

    components: { FormView },

    props: {
      title: {
        type: String,
        default: '查看详情'
      },
      trigger: {
        default: 'click'
      },
      placement: {
        default: 'right-top'
      }
    },

    data() {
      return {
        visible: false
      }
    },

    computed: {
      bodyStyle() {
        return this.visible ? { padding: '20px' } : {}
      }
    },

    methods: {
      toggleVisible() {
        this.visible = !this.visible
      }
    }
  }
</script>
<!--// string string, text-->
<!--// number, 整数，小数，正整数，正小数-->
<!--// date dateTime-->
<!--// enum, 后台枚举，业务枚举-->
<!--// boolean-->
<!--//-->

<!--fields: [-->
<!--  {-->
<!--     type: 'dataType',-->
<!--     props: {-->

<!--     },-->
<!--     on: {-->
<!--       handle-->
<!--     },-->
<!--     rule: {} // 校验,-->
<!--     isShow: true,-->

<!--     // 联动-->
<!--   }-->
<!--]-->

<!--fieldValues: {-->

<!--}-->
