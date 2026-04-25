<template>
  <div>
    <template v-if="visible">
      <el-row
        :gutter="20"
        v-for="(item, index) in fieldsRowLen"
        :key="index">
        <el-col
          :span="colSpan"
          v-for="(item1, colIndex) in fields.slice(index * cols, (index + 1) * cols).length"
          :key="fields[index * cols + colIndex].key">
          <el-form-item
            :label="i18n(fields[ index * cols + colIndex].label)"
            prop="attr1">
            <el-input
              v-if="!readOnly"
              v-model="fieldsValue[fields[index * cols + colIndex].prop]"
              :placeholder="i18n('请输入')"/>
            <span v-else>{{ fieldsValue[fields[index * cols + colIndex].prop] }}</span>
          </el-form-item>
        </el-col>
      </el-row>
    </template>

    <el-button v-if="fields.length > 0" @click="handleToggleVisible" style="margin-left: 120px;">{{ visible ? '收起' : '附加信息' }}</el-button>
  </div>
</template>

<script>
  export default {
    name: 'ExtraFields',

    props: {
      fields: {
        type: Array,
        required: true,
        default() {
          return []
        }
      },

      fieldsValue: {
        type: Object,
        default() {
          return {}
        }
      },
      cols: {
        type: Number,
        default: 2
      },
      readOnly: {
        type: Boolean,
        default: false
      }
    },

    computed: {
      colSpan() {
        return 24 / this.cols
      },
      fieldsRowLen() {
        return Math.ceil(this.fields.length / this.cols)
      }
    },

    data() {
      return {
        visible: false
      }
    },

    methods: {
      handleToggleVisible() {
        this.visible = !this.visible
      }
    }
  }
</script>

<style scoped>

</style>
