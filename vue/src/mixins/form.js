export default {
  methods: {
    resetFields(formName) {
      const $form = this.$refs[formName]
      $form && $form.resetFields()
    }
  }
}
