import {queryColumn} from '@/api/customField'

export const queryExtraColumns = async(tableName) => {
  if (!tableName) {
    return []
  }
  const { rows } = await queryColumn(tableName)
  return rows.filter(item => item.field_name)
    .map(({ field_name, field_mapping }) => {
      return {
        label: field_name,
        key: field_mapping,
        prop: field_mapping,
        width: 140
      }
    })
}

export default {
  methods: {
    /**
     * @deprecated
     * @returns {Promise<void>}
     */
    async queryColumns() {
      await this.queryExtraColumns(this.tableName)
    }
  }
}
