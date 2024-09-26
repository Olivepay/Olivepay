import { useEffect } from 'react';
import { Button } from '../../component/common';
import { CommonProps, CouponOption } from '../../types/donate';
import Select, { StylesConfig, GroupBase, SingleValue } from 'react-select';

const customStyles: StylesConfig<
  CouponOption,
  false,
  GroupBase<CouponOption>
> = {
  control: (provided) => ({
    ...provided,
    height: '3.5rem',
    borderRadius: '30px',
    border: 'none',
    boxShadow:
      '0 4px 6px rgba(50, 50, 93, 0.11), 0 1px 3px rgba(0, 0, 0, 0.08)',
    padding: '0 1rem',
    fontSize: 'text-md',
  }),
  menu: (provided) => ({
    ...provided,
    boxShadow: '0 4px 8px rgba(50, 50, 93, 0.25)',
  }),
  option: (provided, state) => ({
    ...provided,
    padding: '10px 20px',
    backgroundColor: state.isSelected ? '#99BBA2' : 'white',
    color: state.isSelected ? 'white' : 'black',
    fontWeight: 'normal',
  }),
};

const couponMessages: CouponOption[] = [
  { value: 1, label: '✨ 오늘도 즐거운 하루' },
  { value: 2, label: '🌟 행복한 하루 보내세요' },
  { value: 3, label: 'for you 🎁' },
  { value: 4, label: '화이팅 화이팅 화이팅 🥰' },
  { value: 5, label: '🥗 건강한 식사를 위하여' },
  { value: 6, label: '오늘은 왠지 햄버거가 땡기는 날🍔🍟' },
];

const Donate2: React.FC<CommonProps> = ({
  onNext,
  donateInfo,
  setDonateInfo,
}) => {
  const { money, count2000, count4000, couponMessage } = donateInfo;

  useEffect(() => {
    if (!couponMessage) {
      setDonateInfo((prevInfo) => ({
        ...prevInfo,
        couponMessage: couponMessages[0].label,
      }));
    }
  }, [couponMessage, setDonateInfo]);

  const handleCountChange = (
    value: number,
    field: 'count2000' | 'count4000',
  ) => {
    setDonateInfo((prevInfo) => {
      const updatedInfo = {
        ...prevInfo,
        [field]: Math.max(prevInfo[field] + value, 0),
      };
      const newMoney =
        updatedInfo.count2000 * 2000 + updatedInfo.count4000 * 4000;

      return {
        ...updatedInfo,
        money: newMoney,
      };
    });
  };

  const handleCouponChange = (selectedOption: SingleValue<CouponOption>) => {
    if (selectedOption) {
      setDonateInfo((prevInfo) => ({
        ...prevInfo,
        couponMessage: selectedOption.label,
      }));
    }
  };

  return (
    <main className="px-10 py-5">
      <figure className="mb-14 flex flex-col gap-y-10">
        <p className="ml-3 text-md font-semibold text-gray-600">보낼 금액</p>
        <p className="text-center text-2xl font-bold">{`${money.toLocaleString()}원`}</p>
      </figure>

      <figure className="flex flex-col gap-y-8">
        <label className="flex flex-col gap-y-3">
          <span className="ml-3 text-md font-semibold text-gray-600">
            쿠폰 발행 멘트 선택
          </span>
          <div className="mt-2 flex flex-col">
            <Select
              styles={customStyles}
              name="couponMessage"
              value={
                couponMessages.find(
                  (option) => option.label === couponMessage,
                ) || couponMessages[0]
              }
              onChange={handleCouponChange}
              options={couponMessages}
              className="basic-single text-md"
              classNamePrefix="select"
              components={{ IndicatorSeparator: () => null }}
            />
          </div>
        </label>

        <div className="flex flex-col gap-y-2 px-20">
          <p className="text-md font-semibold">2000원 권</p>
          <div className="flex justify-center gap-4 rounded-lg border px-6 py-4">
            <Button
              label="－"
              variant="text"
              onClick={() => handleCountChange(-1, 'count2000')}
            />
            <span className="mx-2 text-lg font-semibold">{count2000}</span>
            <Button
              label="＋"
              variant="text"
              onClick={() => handleCountChange(1, 'count2000')}
            />
          </div>
        </div>
        <div className="flex flex-col gap-y-2 px-20">
          <p className="text-md font-semibold">4000원 권</p>
          <div className="flex justify-center gap-4 rounded-lg border px-6 py-4">
            <Button
              label="－"
              variant="text"
              onClick={() => handleCountChange(-1, 'count4000')}
            />
            <span className="mx-2 text-lg font-semibold">{count4000}</span>
            <Button
              label="＋"
              variant="text"
              onClick={() => handleCountChange(1, 'count4000')}
            />
          </div>
        </div>
      </figure>

      <div className="pb-20 pt-10">
        <Button label="다음으로" variant="primary" onClick={onNext} />
      </div>
    </main>
  );
};

export default Donate2;
