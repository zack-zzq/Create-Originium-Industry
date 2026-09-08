#!/usr/bin/env python3
"""Write gzip-compressed structure NBTs for Ponder scenes (MC 1.21.1)."""
from __future__ import annotations

import gzip
import io
import os
import struct
from pathlib import Path

TAG_END = 0
TAG_BYTE = 1
TAG_SHORT = 2
TAG_INT = 3
TAG_LONG = 4
TAG_FLOAT = 5
TAG_DOUBLE = 6
TAG_BYTE_ARRAY = 7
TAG_STRING = 8
TAG_LIST = 9
TAG_COMPOUND = 10
TAG_INT_ARRAY = 11
TAG_LONG_ARRAY = 12

DATA_VERSION = 3955  # Minecraft 1.21.1


def parse_named_nbt(data: bytes) -> dict:
    """Read a root named compound. Raises if the payload is truncated."""
    return _NbtReader(data).named()[2]


class _NbtReader:
    def __init__(self, data: bytes) -> None:
        self.data = data
        self.i = 0

    def _need(self, n: int) -> None:
        if self.i + n > len(self.data):
            raise EOFError(f"truncated NBT: need {n} byte(s) at {self.i}/{len(self.data)}")

    def u8(self) -> int:
        self._need(1)
        b = self.data[self.i]
        self.i += 1
        return b

    def u16(self) -> int:
        self._need(2)
        v = struct.unpack_from(">H", self.data, self.i)[0]
        self.i += 2
        return v

    def i32(self) -> int:
        self._need(4)
        v = struct.unpack_from(">i", self.data, self.i)[0]
        self.i += 4
        return v

    def i64(self) -> int:
        self._need(8)
        v = struct.unpack_from(">q", self.data, self.i)[0]
        self.i += 8
        return v

    def f32(self) -> float:
        self._need(4)
        v = struct.unpack_from(">f", self.data, self.i)[0]
        self.i += 4
        return v

    def f64(self) -> float:
        self._need(8)
        v = struct.unpack_from(">d", self.data, self.i)[0]
        self.i += 8
        return v

    def string(self) -> str:
        n = self.u16()
        self._need(n)
        s = self.data[self.i : self.i + n].decode("utf-8")
        self.i += n
        return s

    def payload(self, tag: int):
        if tag == TAG_END:
            return None
        if tag == TAG_BYTE:
            return self.u8()
        if tag == TAG_SHORT:
            self._need(2)
            v = struct.unpack_from(">h", self.data, self.i)[0]
            self.i += 2
            return v
        if tag == TAG_INT:
            return self.i32()
        if tag == TAG_LONG:
            return self.i64()
        if tag == TAG_FLOAT:
            return self.f32()
        if tag == TAG_DOUBLE:
            return self.f64()
        if tag == TAG_BYTE_ARRAY:
            n = self.i32()
            self._need(n)
            self.i += n
            return n
        if tag == TAG_STRING:
            return self.string()
        if tag == TAG_LIST:
            child = self.u8()
            n = self.i32()
            return [self.payload(child) for _ in range(n)]
        if tag == TAG_COMPOUND:
            out: dict = {}
            while True:
                child = self.u8()
                if child == TAG_END:
                    return out
                name = self.string()
                out[name] = self.payload(child)
        if tag == TAG_INT_ARRAY:
            n = self.i32()
            self._need(4 * n)
            self.i += 4 * n
            return n
        if tag == TAG_LONG_ARRAY:
            n = self.i32()
            self._need(8 * n)
            self.i += 8 * n
            return n
        raise ValueError(f"unsupported NBT tag {tag}")

    def named(self) -> tuple[int, str, object]:
        tag = self.u8()
        if tag == TAG_END:
            return tag, "", None
        name = self.string()
        return tag, name, self.payload(tag)


class NbtWriter:
    def __init__(self) -> None:
        self.buf = io.BytesIO()

    def raw(self) -> bytes:
        return self.buf.getvalue()

    def write_string(self, s: str) -> None:
        encoded = s.encode("utf-8")
        self.buf.write(struct.pack(">H", len(encoded)))
        self.buf.write(encoded)

    def named(self, tag: int, name: str) -> None:
        self.buf.write(bytes([tag]))
        self.write_string(name)

    def end(self) -> None:
        self.buf.write(bytes([TAG_END]))

    def int_named(self, name: str, value: int) -> None:
        self.named(TAG_INT, name)
        self.buf.write(struct.pack(">i", value))

    def string_named(self, name: str, value: str) -> None:
        self.named(TAG_STRING, name)
        self.write_string(value)

    def list_start(self, name: str, child: int, count: int) -> None:
        self.named(TAG_LIST, name)
        self.buf.write(bytes([child]))
        self.buf.write(struct.pack(">i", count))

    def compound_start(self, name: str) -> None:
        self.named(TAG_COMPOUND, name)

    def int_list(self, name: str, values: list[int]) -> None:
        self.list_start(name, TAG_INT, len(values))
        for v in values:
            self.buf.write(struct.pack(">i", v))

    def unnamed_compound(self, write_body) -> None:
        """Write one TAG_Compound list element.

        List payloads are anonymous: type+name are *not* repeated per element.
        Prefixing ``0x0A 0x00 0x00`` makes the first element swallow the rest of
        the file; Ponder's ``NbtIo.read`` then hits ``EOFException``.
        """
        write_body()
        self.end()


def palette_entry(name: str, props: dict[str, str] | None = None) -> dict:
    return {"Name": name, "Properties": props or {}}


def write_structure(path: Path, size: tuple[int, int, int], palette: list[dict], blocks: list[tuple[tuple[int, int, int], int]]) -> None:
    w = NbtWriter()
    w.compound_start("")
    w.int_list("size", list(size))
    w.list_start("entities", TAG_COMPOUND, 0)

    w.list_start("blocks", TAG_COMPOUND, len(blocks))
    for pos, state in blocks:
        def write_block(pos=pos, state=state) -> None:
            w.int_list("pos", list(pos))
            w.int_named("state", state)

        w.unnamed_compound(write_block)

    w.list_start("palette", TAG_COMPOUND, len(palette))
    for entry in palette:
        def write_palette(entry=entry) -> None:
            w.string_named("Name", entry["Name"])
            props = entry.get("Properties") or {}
            if props:
                w.compound_start("Properties")
                for k, v in props.items():
                    w.string_named(k, v)
                w.end()

        w.unnamed_compound(write_palette)

    w.int_named("DataVersion", DATA_VERSION)
    w.end()

    path.parent.mkdir(parents=True, exist_ok=True)
    raw = w.raw()
    parsed = parse_named_nbt(raw)
    if not isinstance(parsed, dict) or "size" not in parsed or "palette" not in parsed or "blocks" not in parsed:
        raise RuntimeError(f"generated NBT missing structure keys: {path}")
    if not parsed["blocks"] or "pos" not in parsed["blocks"][0] or "state" not in parsed["blocks"][0]:
        raise RuntimeError(f"block list elements must expose pos/state at the top level: {path}")
    if not parsed["palette"] or "Name" not in parsed["palette"][0]:
        raise RuntimeError(f"palette list elements must expose Name at the top level: {path}")

    with gzip.open(path, "wb") as f:
        f.write(raw)
    print(f"wrote {path} ({path.stat().st_size} bytes, {len(blocks)} blocks)")


def plate(size: int, extra: list[tuple[tuple[int, int, int], dict]]) -> tuple[list[dict], list[tuple[tuple[int, int, int], int]]]:
    palette: list[dict] = [palette_entry("minecraft:smooth_stone")]
    index: dict[str, int] = {"minecraft:smooth_stone|": 0}
    blocks: list[tuple[tuple[int, int, int], int]] = []
    for x in range(size):
        for z in range(size):
            blocks.append(((x, 0, z), 0))

    def state_index(entry: dict) -> int:
        props = entry.get("Properties") or {}
        key = entry["Name"] + "|" + ",".join(f"{k}={v}" for k, v in sorted(props.items()))
        if key not in index:
            index[key] = len(palette)
            palette.append(entry)
        return index[key]

    for pos, entry in extra:
        blocks.append((pos, state_index(entry)))
    return palette, blocks


def main() -> None:
    root = Path(__file__).resolve().parents[1] / "src/main/resources/assets/create_originium_industry/ponder"
    size = 7

    # Dust generation: mill + cog + shaft + meter
    pal, blocks = plate(size, [
        ((2, 1, 2), palette_entry("create:millstone")),
        ((3, 1, 2), palette_entry("create:cogwheel", {"axis": "y"})),
        ((4, 1, 2), palette_entry("create:shaft", {"axis": "x"})),
        ((5, 1, 4), palette_entry("create_originium_industry:originium_dust_meter", {"facing": "west"})),
    ])
    write_structure(root / "dust_generation.nbt", (size, 3, size), pal, blocks)

    # Filter / sieve: kinetic filter + shafts + basin sieve
    pal, blocks = plate(size, [
        ((2, 1, 2), palette_entry("create:shaft", {"axis": "z"})),
        ((2, 1, 3), palette_entry("create_originium_industry:originium_dust_filter", {"facing": "south"})),
        ((2, 1, 4), palette_entry("create:shaft", {"axis": "z"})),
        ((5, 1, 3), palette_entry("create:basin")),
        ((4, 1, 3), palette_entry("create_originium_industry:originium_dust_sieve", {"facing": "west"})),
        ((5, 1, 4), palette_entry("create_originium_industry:originium_alloy_sieve", {"facing": "south"})),
        ((1, 1, 5), palette_entry("create:encased_fan", {"facing": "west"})),
        ((0, 1, 5), palette_entry("create_originium_industry:originium_dust_nozzle", {"facing": "west"})),
    ])
    write_structure(root / "dust_filter.nbt", (size, 3, size), pal, blocks)

    # Supercooling / purest: basin + mixer + sieve + cooling chamber
    pal, blocks = plate(size, [
        ((3, 1, 3), palette_entry("create:basin")),
        ((3, 3, 3), palette_entry("create:mechanical_mixer")),
        ((2, 1, 3), palette_entry("create_originium_industry:originium_dust_sieve", {"facing": "west"})),
        ((4, 1, 3), palette_entry("create_originium_industry:originium_cooling_chamber", {"facing": "east"})),
        ((3, 1, 4), palette_entry("minecraft:packed_ice")),
    ])
    write_structure(root / "purest_supercooling.nbt", (size, 5, size), pal, blocks)

    # Power core: housing, shaft, super chamber on top
    pal, blocks = plate(size, [
        ((3, 1, 3), palette_entry("create_originium_industry:originium_power_core", {"facing": "east"})),
        ((3, 1, 2), palette_entry("create_originium_industry:originium_core_housing")),
        ((3, 1, 4), palette_entry("create_originium_industry:originium_alloy_casing")),
        ((2, 1, 3), palette_entry("create_originium_industry:originium_cooling_chamber", {"facing": "west"})),
        ((3, 2, 3), palette_entry("create_originium_industry:originium_super_cooling_chamber", {"facing": "up"})),
        ((4, 1, 3), palette_entry("create:shaft", {"axis": "x"})),
        ((5, 1, 3), palette_entry("create:shaft", {"axis": "x"})),
    ])
    write_structure(root / "power_core.nbt", (size, 4, size), pal, blocks)


if __name__ == "__main__":
    main()
