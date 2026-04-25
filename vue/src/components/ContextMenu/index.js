import Vue from 'vue'
import ContextMenuPopover from './main'
import ContextMenu from './menu'

export {
  ContextMenuPopover,
  ContextMenu
}

let contextMenuPopover
function createMenu() {
  if (contextMenuPopover) {
    return contextMenuPopover
  }

  contextMenuPopover = new Vue(ContextMenuPopover)
  const div = document.createElement('div')
  document.body.appendChild(div)
  contextMenuPopover.$mount(div)
}

Vue.prototype.$_contextMenu = {
  /**
   * 显示contextMenu
   * @param config - contextMenu配置
   * @param config.position
   * @param config.content - 内容
   */
  show(config) {
    createMenu()

    contextMenuPopover.show(config)
    contextMenuPopover.setContent(config.content)
  },

  hide() {
    contextMenuPopover.hide()
  }
}
