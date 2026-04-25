import {getStoreHouse} from '@/api/storage-location'
import LRU from 'lru-cache'
import {getAllHouse} from '@/api/warehouse'

const lru = new LRU(50)

// 暴露的业务类型
export const BizTypes = {
  store: 'store',
  houseCode: 'houseCode'
}

// 业务类型对应的查询接口地址
const BizTypeMethods = {
  store: getStoreHouse,
  house: getAllHouse
}

export default {
  props: {
    bizType: {
      required: true
    }
  },

  data() {
    return {
      list: []
    }
  },

  computed: {
    bizMethod() {
      return BizTypeMethods[this.bizType]
    }
  },

  methods: {
    async getDataWithCache() {
      let p = lru.get(this.bizType)
      if (!p) {
        p = this.bizMethod()
        lru.set(this.bizType, p)
      }
      return p
    },

    async getData() {
      // {rows: []}
      return this.getDataWithCache()
    }
  }

}
