import {loading} from '@/decorator'

export default {
  data() {
    return {
      visible: false,
      model: {},
      loading: false,
      formName: 'editForm',
      modalTableHeight: 'calc(100vh - 400px)',
      modalTableWidth: 'calc(100vw - 200px)',
      defaultWarehouse: ''
    }
  },

  methods: {
    add(row) {
      this.edit(row)
    },

    edit(row = {}) {
      this.resetFields()
      this.visible = true

      this.$nextTick(() => {
        this.model = {
          ...row
        }

        if (this.searchFields && this.params) {
          Object.keys(this.searchFields).forEach(field => {
            const fieldProp = this.searchFields[field].prop
            if (this.model[fieldProp] !== undefined) {
              this.$set(this.params, fieldProp, this.model[fieldProp])
            }
          })
        }

        this.afterEdit()
      })
    },

    select() {
      this.$nextTick(() => {
        this.visible = true
        this.afterEdit()
      })
    },

    afterEdit() {},

    handleCancel() {
      this.beforeCancel()
      this.visible = false
      this.clearSelectionKeys && this.clearSelectionKeys()
    },

    beforeCancel() {

    },

    @loading('loading')
    async handleSubmit() {
      try {
        if (this.$refs[this.formName]) {
          await this.$refs[this.formName].validate()
        }
        await this.submit()

        this.handleCancel()
        this.$emit('ok')
      } catch (e) {
        console.log(e)
      }
    },

    resetFields() {
      const $form = this.$refs[this.formName]
      if (!$form) {
        return
      }
      $form.resetFields()
    },

    handleWarehouseInit(options) {
      if (options.length === 1) {
        this.defaultWarehouse = options[0].house_no || ''
        this.$set(this.model, 'house_code', this.defaultWarehouse)
      }
    },

    setWarehouse() {
      this.$set(this.model, 'house_code', this.defaultWarehouse)
    }
  }
}
