from __future__ import annotations

import re
from datetime import datetime

from pydantic import BaseModel, ConfigDict, EmailStr, Field, field_validator


PHONE_PATTERN = re.compile(r"^\+?[0-9][0-9\s().-]*$")


def _normalize_phone_digits(value: str) -> int:
    return len(re.sub(r"\D", "", value))


class ContactBase(BaseModel):
    model_config = ConfigDict(str_strip_whitespace=True)

    name: str = Field(..., max_length=255, min_length=1)
    phone_number: str
    email: EmailStr | None = None
    address: str | None = None

    @field_validator("email", "address", mode="before")
    @classmethod
    def empty_string_to_none(cls, value):
        if value is None:
            return None
        if isinstance(value, str) and not value.strip():
            return None
        return value

    @field_validator("phone_number")
    @classmethod
    def validate_phone_number(cls, value: str) -> str:
        if not value:
            raise ValueError("Phone number is required.")
        if not PHONE_PATTERN.fullmatch(value) or _normalize_phone_digits(value) != 10:
            raise ValueError("Phone number must contain exactly 10 digits.")
        return value

    @field_validator("name")
    @classmethod
    def validate_name(cls, value: str) -> str:
        if any(character.isdigit() for character in value):
            raise ValueError("Name cannot contain numbers.")
        return value


class ContactCreate(ContactBase):
    pass


class ContactRead(BaseModel):
    model_config = ConfigDict(from_attributes=True, str_strip_whitespace=True)

    name: str
    phone_number: str
    email: EmailStr | None = None
    address: str | None = None
    id: int
    created_at: datetime
