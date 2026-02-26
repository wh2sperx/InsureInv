# InsureInv

<div align="center">

# InsureInv

[![Codeberg](https://img.shields.io/badge/hosted-Codeberg-2185D0?logo=codeberg)](https://codeberg.org/qhuy1123/InsureInv)
[![Modrinth](https://img.shields.io/badge/available-Modrinth-00AF5C?logo=modrinth)](https://modrinth.com/plugin/insureinv)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)
[![hyticallib-i18n](https://img.shields.io/badge/dependency-JitPack-6B8E4C)](https://jitpack.io/#org.codeberg.qhuy1123/hyticallib-i18n)

Plugin bảo vệ inventory cho Minecraft server. Dựa trên hệ thống charge có thể mua bằng tiền in-game. Hỗ trợ Folia và Paper 1.21+.

</div>

## Storage

Plugin hỗ trợ 3 loại lưu trữ dữ liệu:

- **SQLite** — mặc định, không cần cấu hình thêm
- **MySQL** — dùng HikariCP connection pool, cấu hình trong `config.yml`
- **JSON** — lưu file JSON đơn giản

```yaml
storage:
  method: sqlite  # sqlite | mysql | json
```

## Economy API

Plugin tích hợp với các hệ thống economy thông qua `economy.provider` trong `config.yml`:

| Provider | Mô tả |
|---|---|
| `VAULT` | Sử dụng Vault API (mặc định) |
| `PLAYER_POINTS` | Sử dụng PlayerPoints |
| `NONE` | Tắt tính năng economy, mọi giao dịch đều bị từ chối |

```yaml
economy:
  provider: VAULT
  price-per-charge: 100.0
  max-charges-per-player: 10
```

## Commands

Lệnh chính: `/insureinv` (alias: `/inv`, `/hinv`)

| Lệnh | Mô tả | Quyền |
|---|---|---|
| `/insureinv buy <số lượng>` | Mua charge bảo vệ inventory | `insureinv.use` |
| `/insureinv toggle [player]` | Bật/tắt bảo vệ inventory | `insureinv.use` |
| `/insureinv info [player]` | Xem thông tin charge và trạng thái | `insureinv.use` |
| `/insureinv help` | Hiển thị danh sách lệnh | `insureinv.use` |
| `/insureinv set <player> <số lượng>` | Đặt số charge cho người chơi | `insureinv.admin` |
| `/insureinv setprice <giá>` | Thay đổi giá mỗi charge | `insureinv.admin` |
| `/insureinv setmax <số>` | Thay đổi charge tối đa | `insureinv.admin` |
| `/insureinv setlang <mã ngôn ngữ>` | Đặt ngôn ngữ cho bản thân | `insureinv.use` |
| `/insureinv langreload` | Reload file ngôn ngữ | `insureinv.admin` |
| `/insureinv reload` | Reload config plugin | `insureinv.admin` |
| `/insureinv version` | Xem thông tin phiên bản và build | `insureinv.admin` |

## Permissions

| Quyền | Mô tả | Mặc định |
|---|---|---|
| `insureinv.use` | Sử dụng các lệnh cơ bản (buy, toggle, info, help, setlang) | `true` |
| `insureinv.admin` | Sử dụng các lệnh quản trị (set, setprice, setmax, reload, langreload, version) | `op` |

`insureinv.admin` kế thừa `insureinv.use`.

## Metrics

Plugin sử dụng [bStats](https://bstats.org/) để thu thập dữ liệu ẩn danh (phiên bản server, số người chơi, ...).

Để tắt metrics, mở `config.yml` và sửa:

```yaml
metrics:
  enabled: false
```

## License

Plugin được phân phối theo giấy phép [MIT](LICENSE).
