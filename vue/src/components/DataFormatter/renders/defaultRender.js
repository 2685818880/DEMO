export function defaultRender(h, proxy) {
  return (
    <span>{proxy.formatter.data}</span>
  )
}
