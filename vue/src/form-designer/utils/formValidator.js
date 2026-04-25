/**
 * 表单校验工具
 * 提供表单设计器中常用的校验规则和方法
 */

/**
 * 校验JSON配置格式是否正确
 * @param {Object} config 表单配置对象
 * @returns {{ valid: boolean, errors: string[] }}
 */
export function validateFormConfig(config) {
  const errors = []

  if (!config || typeof config !== 'object') {
    errors.push('表单配置不能为空')
    return { valid: false, errors }
  }

  if (!config.fields || !Array.isArray(config.fields)) {
    errors.push('表单配置缺少字段列表(fields)')
    return { valid: false, errors }
  }

  config.fields.forEach((field, index) => {
    const fieldErrors = validateField(field, index)
    errors.push(...fieldErrors)
  })

  return {
    valid: errors.length === 0,
    errors
  }
}

/**
 * 校验单个字段配置
 * @param {Object} field 字段配置
 * @param {number} index 字段索引
 * @returns {string[]}
 */
export function validateField(field, index) {
  const errors = []
  const config = field.__config__

  if (!config) {
    errors.push(`字段 #${index + 1}: 缺少__config__配置`)
    return errors
  }

  if (!config.tag) {
    errors.push(`字段 #${index + 1}: 缺少组件标签(tag)`)
  }

  if (config.required && !field.__vModel__ && config.layout === 'colFormItem') {
    errors.push(`字段 "${config.label || index + 1}": 必填字段缺少字段名(__vModel__)`)
  }

  if (config.layout === 'colFormItem' && config.span !== undefined) {
    if (config.span < 1 || config.span > 24) {
      errors.push(`字段 "${config.label || index + 1}": 栅格跨度(span)必须在1-24之间`)
    }
  }

  // 校验子组件
  if (config.children && Array.isArray(config.children)) {
    config.children.forEach((child, childIndex) => {
      const childErrors = validateField(child, `${index}.${childIndex}`)
      errors.push(...childErrors)
    })
  }

  return errors
}

/**
 * 校验表单数据
 * @param {Object} formConfig 表单配置
 * @param {Object} formData 表单数据
 * @returns {{ valid: boolean, errors: Array<{ field: string, message: string }> }}
 */
export function validateFormData(formConfig, formData) {
  const errors = []

  if (!formConfig || !formConfig.fields) {
    return { valid: true, errors }
  }

  formConfig.fields.forEach(field => {
    const config = field.__config__
    if (!config || !config.required) return

    const vModel = field.__vModel__
    if (!vModel) return

    const value = formData[vModel]
    if (value === undefined || value === null || value === '') {
      errors.push({
        field: vModel,
        message: `${config.label || vModel}不能为空`
      })
    }

    // 数组类型校验（多选、级联等）
    if (config.required && Array.isArray(value) && value.length === 0) {
      errors.push({
        field: vModel,
        message: `请至少选择一个${config.label || vModel}`
      })
    }
  })

  return {
    valid: errors.length === 0,
    errors
  }
}

/**
 * 获取字段的默认值
 * @param {Object} fieldConfig 字段配置
 * @returns {*}
 */
export function getFieldDefaultValue(fieldConfig) {
  const config = fieldConfig.__config__
  if (!config) return undefined

  if (config.defaultValue !== undefined) {
    return config.defaultValue
  }

  // 根据组件类型提供默认值
  const defaultValues = {
    'el-input': '',
    'el-input-number': undefined,
    'el-select': '',
    'el-radio-group': undefined,
    'el-checkbox-group': [],
    'el-switch': false,
    'el-slider': 0,
    'el-time-picker': null,
    'el-date-picker': null,
    'el-rate': 0,
    'el-color-picker': null,
    'el-upload': [],
    'el-cascader': []
  }

  return defaultValues[config.tag] !== undefined ? defaultValues[config.tag] : ''
}
