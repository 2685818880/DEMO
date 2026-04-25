<template>
  <el-form
    @submit="handleSubmit"
    inline
    :label-width="labelWidth">
    <el-form-item
      v-for="(field, index) in fields"
      :key="index"
      v-show="index < visibleNum || moreVisible"
      :label="field.label">
      <template v-if="isMultiProp(field.prop)">
        <field-comp
          :name="field.type"
          v-bind="{...field.props, ...getMultiPropBind(field.prop)}"
          v-on="{...field.on, ...getMultiPropAsync(field.prop)}">
        </field-comp>
      </template>
      <field-comp
        v-else
        @keydown.native.enter="handleSubmit"
        :name="field.type"
        v-model.trim="fieldsValue[field.prop]"
        v-bind="field.props"
        v-on="field.on" />
    </el-form-item>
    <el-form-item>
      <el-button
        icon="el-icon-search"
        type="primary"
        @click="handleSubmit">
        查询
      </el-button>
      <el-button
        icon="el-icon-refresh"
        @click="handleReset">
        重置
      </el-button>
      <slot></slot>
      <el-button
        :icon="moreIcon"
        @click="toggleVisible"
        v-if="isShowMore">{{ moreVisible ? '收起' : '展开' }}
      </el-button>
    </el-form-item>
  </el-form>
</template>

<script>
export default {
  name: 'SearchForm',

  props: {
    labelWidth: {
      default: '110px'
    },
    fields: {
      type: Array,
      required: true
    },
    fieldsValue: {
      type: Object,
      required: true
    },
    visibleNum: {
      default: 2
    }
  },

  computed: {
    isShowMore() {
      return this.fields.length > this.visibleNum
    },
    moreIcon() {
      return this.moreVisible ? 'el-icon-arrow-up' : 'el-icon-arrow-down'
    }
  },

  data() {
    return {
      moreVisible: false
    }
  },

  methods: {
    handleSubmit() {
      this.$emit('enter')
    },
    handleReset() {
      this.$emit('reset')
    },
    toggleVisible() {
      this.moreVisible = !this.moreVisible
    },

    isMultiProp(prop) {
      return typeof prop === 'object'
    },

    getMultiPropBind(props) {
      const res = {}
      Object.keys(props).forEach(key => {
        res[key] = this.fieldsValue[props[key]]
      })
      return res
    },

    getMultiPropAsync(props) {
      const res = {}
      Object.keys(props).forEach(key => {
        res[`update:${key}`] = (val) => {
          this.$set(this.fieldsValue, props[key], val)
        }
      })
      return res
    }
  }
}
</script>

<style lang="scss" scoped>
//.g-form-flex {
//  &.el-form--inline .el-form-item {
//    display: flex;
//
//    .el-form-item__content {
//      flex: 1;
//    }
//  }
//}

>>> .el-form-item--mini.el-form-item, .el-form-item--small.el-form-item {
  margin-bottom: 5px;
}
</style>
