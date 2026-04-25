import StorageMap from './src/index'

StorageMap.install = (Vue) => {
  Vue.component(StorageMap.name, StorageMap)
}

export default StorageMap
