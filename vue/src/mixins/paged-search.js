import fetch from '@/utils/fetch'
import { getExtraFieldList } from '@/api/extra-field'
import { ExtraFieldGroup } from '@/utils/enums'

export default {
  data() {
    return {
      pagination: {
        page: 1,
        row: 20,
        total: 0
      },
      params: {},
      isCreatedLoadData: true,
      loading: false,
      list: [],
      selectionKeys: [],
      selectionRows: [],
      paramsCache: {},
      showColumns: this.columns || [],
      extra_business_type: '',
      resetModelBeforeSearch: false,
      itemAlive: true
    }
  },

  async created() {
    await this.generateFinalColumns()
    await this.getExtraFields()

    if (this.isCreatedLoadData) {
      await this.loadData()
    }

    this.paramsCache = {
      ...this.param
    }

    console.log(this.$options.name)
  },

  computed: {
    hasSelected() {
      return this.selectionKeys.length > 0
    }
  },

  methods: {
    onColumnChange(columns) {
      this.showColumns = columns
    },

    async getExtraFields() {
      const group = ExtraFieldGroup[this.$options.name]
      if (group) {
        const params = {
          page: 1,
          row: 100,
          extra_field_type: group,
          extra_business_type: this.extra_business_type
        }
        const { rows } = await getExtraFieldList(params)
        this.columns && this.columns.push(...rows.map(item => {
          const map = JSON.parse(item.mapping_relations)
          const col = {
            label: item.extra_field_name,
            prop: item.extra_field_code,
            key: item.extra_field_code,
            width: 140,
            attrs: {
              showOverflowTooltip: true
            }
          }
          col.extra_data_type === 'Enum' && (col.format = (val) => {
            return map[val] || '-'
          })
          return col
        }))
      }
    },

    async generateFinalColumns() {
      if (!this.tableName) {
        return
      }

      const actionIndex = this.columns.findIndex((item) => item.key === 'action')
      if (actionIndex === -1) {
        // return
      }
    },

    async loadData() {
      if (!this.pagedUrl) {
        console.warn('请补充pagedUrl属性')
        return
      }

      if (this.resetModelBeforeSearch) {
        this.itemAlive = false
        await this.$nextTick()
        this.itemAlive = true
      }

      let pagedUrl
      if (typeof this.pagedUrl === 'function') {
        pagedUrl = this.pagedUrl()
      } else {
        pagedUrl = this.pagedUrl
      }

      this.loading = true
      const params = this.generateQueryParams()
      try {
        const {rows, total, page, row} = await fetch.get(pagedUrl, {params})
        this.list = rows.map((item, index) => {
          const baseMap = this.handleBaseMap(item, index)
          const extraMap = item.extraMap || {}
          return {
            ...baseMap,
            ...extraMap
          }
        })
        this.pagination = {
          total,
          row,
          page
        }
        this.loading = false
      } catch (e) {
        console.error(e)
        this.loading = false
      }
    },

    generateQueryParams() {
      const {page, row} = this.pagination
      return {
        ...this.params,
        page,
        row
      }
    },

    handleBaseMap(row) {
      return row
    },

    handleAdd(row) {
      this.$refs.modalForm.add(row)
    },

    handleEdit(row) {
      this.$refs.modalForm.edit(row)
    },

    beforeSearch() {},

    handleSearch() {
      this.beforeSearch()
      this.pagination.page = 1
      this.loadData().then(() => {})
      this.clearSelectionKeys()
    },

    handleReload() {
      this.clearSelectionKeys()
      this.loadData().then(() => {})
    },

    handleSizeChange(val) {
      this.pagination.row = val
      this.loadData().then(() => {})
    },

    handlePageChange(val) {
      this.pagination.page = val
      this.loadData().then(() => {})
    },
    handleReset() {
      this.params = {
        ...this.paramsCache
      }
    },

    clearSelectionKeys() {
      this.selectionKeys = []
      this.selectionRows = []
    }
  }
}
