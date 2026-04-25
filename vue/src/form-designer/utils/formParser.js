/**
 * 表单解析工具
 * 提供表单配置的序列化和反序列化功能
 */

/**
 * 将表单配置序列化为JSON字符串
 * @param {Object} formConfig 表单配置对象
 * @param {boolean} pretty 是否格式化
 * @returns {string}
 */
export function serializeFormConfig(formConfig, pretty = false) {
  try {
    const space = pretty ? 2 : 0
    return JSON.stringify(formConfig, (key, value) => {
      // 过滤掉运行时状态
      if (key === 'renderKey' || key === 'formId') return undefined
      return value
    }, space)
  } catch (e) {
    console.error('序列化表单配置失败:', e)
    return null
  }
}

/**
 * 从JSON字符串反序列化为表单配置
 * @param {string} jsonStr JSON字符串
 * @returns {Object|null}
 */
export function deserializeFormConfig(jsonStr) {
  try {
    const config = JSON.parse(jsonStr)

    // 重新生成运行时ID
    if (config.fields && Array.isArray(config.fields)) {
      let idCounter = 100
      regenerateIds(config.fields, () => ++idCounter)
    }

    return config
  } catch (e) {
    console.error('反序列化表单配置失败:', e)
    return null
  }
}

/**
 * 递归重新生成组件ID
 * @param {Array} fields 字段列表
 * @param {Function} idGenerator ID生成器
 */
function regenerateIds(fields, idGenerator) {
  fields.forEach(field => {
    const config = field.__config__
    if (config) {
      config.formId = idGenerator()
      config.renderKey = `${config.formId}${Date.now()}`
    }
    if (config && config.children && Array.isArray(config.children)) {
      regenerateIds(config.children, idGenerator)
    }
  })
}

/**
 * 解析表单配置为初始数据模型
 * @param {Object} formConfig 表单配置
 * @returns {Object}
 */
export function parseFormModel(formConfig) {
  const model = {}

  if (!formConfig || !formConfig.fields) {
    return model
  }

  function extractFields(fields) {
    fields.forEach(field => {
      const vModel = field.__vModel__
      const config = field.__config__

      if (vModel && config) {
        model[vModel] = config.defaultValue !== undefined
          ? config.defaultValue
          : getDefaultByTag(config.tag)
      }

      if (config && config.children && Array.isArray(config.children)) {
        extractFields(config.children)
      }
    })
  }

  extractFields(formConfig.fields)
  return model
}

/**
 * 根据标签获取默认值
 * @param {string} tag 组件标签
 * @returns {*}
 */
function getDefaultByTag(tag) {
  const defaults = {
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
  return defaults[tag] !== undefined ? defaults[tag] : ''
}

/**
 * 导出表单配置为可传输的格式
 * @param {Object} formConfig 表单配置
 * @returns {Object}
 */
export function exportFormConfig(formConfig) {
  return {
    version: '1.0',
    exportTime: new Date().toISOString(),
    formRef: formConfig.formRef,
    formModel: formConfig.formModel,
    size: formConfig.size,
    labelPosition: formConfig.labelPosition,
    labelWidth: formConfig.labelWidth,
    formRules: formConfig.formRules,
    gutter: formConfig.gutter,
    disabled: formConfig.disabled,
    span: formConfig.span,
    formBtns: formConfig.formBtns,
    fields: JSON.parse(JSON.stringify(formConfig.fields))
  }
}
